package com.taskflow.backend.services;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.management.relation.RoleNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.taskflow.backend.dto.CreateUserDto;
import com.taskflow.backend.dto.CredentialsDto;
import com.taskflow.backend.dto.SignUpDto;
import com.taskflow.backend.dto.UpdatePasswordDto;
import com.taskflow.backend.dto.UserDto;
import com.taskflow.backend.entities.Image;
import com.taskflow.backend.entities.Rol;
import com.taskflow.backend.entities.User;
import com.taskflow.backend.enums.RoleTypeEnum;
import com.taskflow.backend.exception.EmailNotFoundException;
import com.taskflow.backend.exception.InvalidCredentialsException;
import com.taskflow.backend.exception.JwtAuthenticationException;
import com.taskflow.backend.exception.PasswordValidationException;
import com.taskflow.backend.exception.UserAlreadyExistsException;
import com.taskflow.backend.mappers.ImageMapper;
import com.taskflow.backend.mappers.RoleMapper;
import com.taskflow.backend.repositories.ImageRepository;
import com.taskflow.backend.repositories.RolRepository;
import com.taskflow.backend.repositories.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RolRepository rolRepository;
    private final ImageRepository imageRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final TokenService tokenService;
    private final EmailService emailService;
    private final ImageMapper imageMapper;
    private final RoleMapper roleMapper;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No se ha encontrado un usuario con ese email: " + email));

        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole() != null ? user.getRole().getRolName().name() : "ROLE_NORMUSER")));
    }

    public UserDto login(@NonNull CredentialsDto credentialsDto) {
        User user = userRepository.findByEmail(credentialsDto.getEmail())
                .orElseThrow(() -> new EmailNotFoundException("No se ha encontrado un usuario con ese email: " + credentialsDto.getEmail()));

        if (!passwordEncoder.matches(credentialsDto.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Error al iniciar sesión, el correo electrónico o la contraseña no son correctas");
        }

        if (!user.getUserVerified()) {
            throw new IllegalArgumentException("El usuario no está verificado, por favor revise su correo electrónico");
        }

        logger.info("Usuario logueado con éxito: {}", user);

        return mapToUserDto(user);
    }

    @Transactional
    public UserDto register(@NonNull SignUpDto signUpDto) throws RoleNotFoundException {
        logger.info("Iniciando el registro del usuario: {}", signUpDto);

        // Validación de la contraseña
        if (!signUpDto.getPassword().equals(signUpDto.getConfirmPassword())) {
            throw new InvalidCredentialsException("Las contraseñas no coinciden"); // Usando la excepción personalizada
        }

        // Validar campos únicos antes de continuar.
        validateUniqueFields(signUpDto);

        // Obtener el rol `ROLE_NORMUSER`.
        Rol roleNormUser = rolRepository.findByRolName(RoleTypeEnum.ROLE_NORMUSER)
                .orElseThrow(() -> {
                    logger.error("Rol `ROLE_NORMUSER` no encontrado en la base de datos");
                    return new RoleNotFoundException("El rol `ROLE_NORMUSER` no se ha encontrado");
                });

        // Generar el token de activación y la fecha de expiración.
        String activationToken = tokenService.generateActivationToken();
        Instant tokenExpiration = tokenService.getTokenExpiration();

        // Crear el nuevo usuario.
        User newUser = User.builder()
                .name(signUpDto.getName())
                .firstSurname(signUpDto.getFirstSurname())
                .secondSurname(signUpDto.getSecondSurname())
                .idCard(signUpDto.getIdCard())
                .phoneNumber(signUpDto.getPhoneNumber())
                .idImage(null) // Enviar el valor de la imagen a null, ya que no se ha asignado una imagen al usuario.
                .role(roleNormUser) // Asigna el rol `ROLE_NORMUSER` automáticamente.
                .email(signUpDto.getEmail())
                .password(passwordEncoder.encode(signUpDto.getPassword()))
                .userVerified(false)
                .status(true) // Al crear el registro se asigna el estado activo.
                .activationToken(activationToken)
                .activationTokenExpiration(tokenExpiration)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        logger.info("Nuevo usuario creado: {}", newUser);

        // Guardar el nuevo usuario.
        User savedUser = userRepository.save(newUser);
        logger.info("Usuario guardado en la base de datos: {}", savedUser);

        // Enviar el correo de activación.
        String activationLink = "http://localhost:8080/auth/activate?token=" + activationToken;
        emailService.sendActivationEmail(savedUser.getEmail(), activationLink);
        logger.info("Correo de activación enviado a: {}", savedUser.getEmail());

        // Mapear el usuario guardado a UserDto y retornarlo.
        return mapToUserDto(savedUser);
    }

    public UserDto findByEmail(@NonNull String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No se ha encontrado un usuario con ese email: " + email));
        return mapToUserDto(user);
    }

    public boolean activateUser(String token) {
        User user = userRepository.findByActivationToken(token);
        if (user == null) {
            throw new JwtAuthenticationException("Token inválido"); // Cambiado a JwtAuthenticationException
        }
        if (user.getActivationTokenExpiration().isBefore(Instant.now())) {
            throw new JwtAuthenticationException("Token expirado"); // Cambiado a JwtAuthenticationException
        }

        user.setUserVerified(true);
        user.clearActivationToken(); // Uso de tu método para limpiar
        userRepository.save(user);

        return true; // Activación exitosa
    }

    public boolean updatePassword(UpdatePasswordDto updatePasswordDto) {
        logger.info("Iniciando actualización de contraseña para el usuario: {}", updatePasswordDto.getEmail());

        // Buscar al usuario
        User user = userRepository.findByEmail(updatePasswordDto.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("No se ha encontrado un usuario con ese email: " + updatePasswordDto.getEmail()));

        // Verificar si las contraseñas coinciden
        if (!updatePasswordDto.getNewPassword().equals(updatePasswordDto.getRepeatNewPassword())) {
            throw new PasswordValidationException("Las contraseñas no coinciden", "PASSWORD_MISMATCH");
        }

        // Actualizar la contraseña
        user.setPassword(passwordEncoder.encode(updatePasswordDto.getNewPassword()));
        userRepository.save(user);

        logger.info("Contraseña actualizada exitosamente para el usuario: {}", updatePasswordDto.getEmail());
        return true;
    }

    public List<UserDto> findAll() {
        logger.info("Usuarios obtenidos: {}", userRepository.findAll());
        return userRepository.findAll().stream()
                .map(this::mapToUserDto)
                .collect(Collectors.toList());
    }

    public UserDto findById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se ha encontrado un usuario con ese ID: " + id));
        return mapToUserDto(user);
    }

    @Transactional
    public UserDto update(Integer id, CreateUserDto updatedUserDto) {
        logger.info("Iniciando la actualización del usuario con ID: {}", id);

        // Buscar el usuario por ID
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se ha encontrado un usuario con ese ID: " + id));

        // Validación de ID de imagen y rol
        if (updatedUserDto.getIdImage() == null || updatedUserDto.getIdRol() == null) {
            logger.error("Los IDs de imagen y rol no pueden ser nulos: idImage={}, idRol={}", updatedUserDto.getIdImage(), updatedUserDto.getIdRol());
            throw new IllegalArgumentException("Los IDs de imagen y rol no pueden ser nulos");
        }

        // Validar campos únicos antes de continuar
        validateUniqueFieldsForUpdate(user, updatedUserDto);

        // Obtener la imagen
        Image image = imageRepository.findById(updatedUserDto.getIdImage())
                .orElseThrow(() -> {
                    logger.error("Imagen no encontrada con ID: {}", updatedUserDto.getIdImage());
                    return new RuntimeException("La imagen no se ha encontrado");
                });

        // Obtener el rol
        Rol rol = rolRepository.findById(updatedUserDto.getIdRol())
                .orElseThrow(() -> {
                    logger.error("Rol no encontrado con ID: {}", updatedUserDto.getIdRol());
                    return new RuntimeException("El rol no se ha encontrado");
                });

        // Actualizar los campos del usuario
        user.setName(updatedUserDto.getName());
        user.setFirstSurname(updatedUserDto.getFirstSurname());
        user.setSecondSurname(updatedUserDto.getSecondSurname());
        user.setIdCard(updatedUserDto.getIdCard());
        user.setPhoneNumber(updatedUserDto.getPhoneNumber());
        user.setIdImage(image);
        user.setRole(rol);
        user.setEmail(updatedUserDto.getEmail());
        user.setUpdatedAt(Instant.now());

        // Guardar el usuario actualizado en la base de datos
        User updatedUser = userRepository.save(user);
        logger.info("Usuario actualizado: {}", updatedUser);

        // Mapear el usuario actualizado a UserDto y retornarlo
        return mapToUserDto(updatedUser);
    }

    private UserDto mapToUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .firstSurname(user.getFirstSurname())
                .secondSurname(user.getSecondSurname())
                .idCard(user.getIdCard())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .idImage(user.getIdImage() != null ? imageMapper.toImageDto(user.getIdImage()) : null) // Mapeo de imagen
                .role(user.getRole() != null ? roleMapper.toRoleDto(user.getRole()) : null) // Mapeo de rol
                .userVerified(user.getUserVerified())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private void validateUniqueFields(SignUpDto signUpDto) {
        if (userRepository.existsByEmail(signUpDto.getEmail())) {
            throw new UserAlreadyExistsException("Ya existe un usuario con ese correo electrónico: " + signUpDto.getEmail());
        }
        if (userRepository.existsByIdCard(signUpDto.getIdCard())) {
            throw new UserAlreadyExistsException("Ya existe un usuario con esa cédula: " + signUpDto.getIdCard());
        }
        if (userRepository.existsByPhoneNumber(signUpDto.getPhoneNumber())) {
            throw new UserAlreadyExistsException("Ya existe un usuario con ese número de teléfono: " + signUpDto.getPhoneNumber());
        }
    }

    // Método para validar campos únicos para crear un usuario con createUserDto.
    private void validateUniqueFieldsForCreateUser(CreateUserDto createUserDto) {
        if (userRepository.existsByEmail(createUserDto.getEmail())) {
            throw new UserAlreadyExistsException("Ya existe un usuario con ese correo electrónico asignado: " + createUserDto.getEmail());
        }
        if (userRepository.existsByIdCard(createUserDto.getIdCard())) {
            throw new UserAlreadyExistsException("Ya existe un usuario con esa cédula asignada: " + createUserDto.getIdCard());
        }
        if (userRepository.existsByPhoneNumber(createUserDto.getPhoneNumber())) {
            throw new UserAlreadyExistsException("Ya existe un usuario con ese número de teléfono asignado: " + createUserDto.getPhoneNumber());
        }
    }

    // Método para validar campos únicos al actualizar un usuario con CreateUserDto.
    private void validateUniqueFieldsForUpdate(User existingUser, CreateUserDto updatedUserDto) {
        if (!existingUser.getEmail().equals(updatedUserDto.getEmail())
                && userRepository.existsByEmail(updatedUserDto.getEmail())) {
            throw new UserAlreadyExistsException("Ya existe un usuario con ese correo electrónico asignado: " + updatedUserDto.getEmail());
        }
        if (!existingUser.getIdCard().equals(updatedUserDto.getIdCard())
                && userRepository.existsByIdCard(updatedUserDto.getIdCard())) {
            throw new UserAlreadyExistsException("Ya existe un usuario con esa cédula asignada: " + updatedUserDto.getIdCard());
        }
        if (!existingUser.getPhoneNumber().equals(updatedUserDto.getPhoneNumber())
                && userRepository.existsByPhoneNumber(updatedUserDto.getPhoneNumber())) {
            throw new UserAlreadyExistsException("Ya existe un usuario con ese número de teléfono asignado: " + updatedUserDto.getPhoneNumber());
        }
    }

    //Método para eliminar un registro de usuario.
    public void delete(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se ha encontrado un usuario con ese ID: " + id));
        userRepository.delete(user);
        logger.info("Usuario eliminado: {}", user);
    }

    @Transactional
    public UserDto createUser(@NonNull CreateUserDto createUserDto) {
        logger.info("Iniciando la creación del usuario: {}", createUserDto);

        // Validación de ID de imagen y rol.
        if (createUserDto.getIdImage() == null || createUserDto.getIdRol() == null) {
            logger.error("Los IDs de imagen y rol no pueden ser nulos: idImage={}, idRol={}", createUserDto.getIdImage(), createUserDto.getIdRol());
            throw new IllegalArgumentException("Los IDs de imagen y rol no pueden ser nulos");
        }

        // Validar campos únicos antes de continuar
        validateUniqueFieldsForCreateUser(createUserDto);

        // Obtener la imagen
        Image image = imageRepository.findById(createUserDto.getIdImage())
                .orElseThrow(() -> {
                    logger.error("Imagen no encontrada con ID: {}", createUserDto.getIdImage());
                    return new RuntimeException("La imagen no se ha encontrado");
                });

        // Obtener el rol
        Rol rol = rolRepository.findById(createUserDto.getIdRol())
                .orElseThrow(() -> {
                    logger.error("Rol no encontrado con ID: {}", createUserDto.getIdRol());
                    return new RuntimeException("El rol no se ha encontrado");
                });

        // Crear el nuevo usuario con el rol especificado
        User newUser = User.builder()
                .name(createUserDto.getName())
                .firstSurname(createUserDto.getFirstSurname())
                .secondSurname(createUserDto.getSecondSurname())
                .idCard(createUserDto.getIdCard())
                .phoneNumber(createUserDto.getPhoneNumber())
                .idImage(image)
                .role(rol)
                .email(createUserDto.getEmail())
                .password(passwordEncoder.encode(createUserDto.getPassword()))
                .userVerified(true) // El usuario creado por un Administrador, ya está verificado
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        logger.info("Nuevo usuario creado: {}", newUser);

        // Guardar el usuario
        User savedUser = userRepository.save(newUser);
        logger.info("Usuario guardado en la base de datos: {}", savedUser);

        // Generar el token de activación y la fecha de expiración
        String activationToken = tokenService.generateActivationToken();
        savedUser.setActivationToken(activationToken);
        savedUser.setActivationTokenExpiration(tokenService.getTokenExpiration());

        // Actualizar el usuario con el token de activación
        userRepository.save(savedUser);
        String activationLink = "http://localhost:8080/auth/activate?token=" + activationToken;
        emailService.sendActivationEmail(savedUser.getEmail(), activationLink);
        logger.info("Correo de activación enviado a: {}", savedUser.getEmail());

        // Mapear el usuario guardado a UserDto y retornarlo
        return mapToUserDto(savedUser);
    }

    public boolean isUserAuthenticated(String email) {
        UserDto user = findByEmail(email);
        return user != null && user.getUserVerified();
    }

}
