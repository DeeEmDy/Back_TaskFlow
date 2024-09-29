package com.taskflow.backend.services;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.taskflow.backend.dto.CredentialsDto;
import com.taskflow.backend.dto.SignUpDto;
import com.taskflow.backend.dto.UserDto;
import com.taskflow.backend.entities.Image;
import com.taskflow.backend.entities.Rol;
import com.taskflow.backend.entities.User;
import com.taskflow.backend.mappers.ImageMapper; // Ajuste aquí
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
    private final ImageMapper imageMapper; // Mapeador de Image
    private final RoleMapper roleMapper;   // Mapeador de Rol

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No se ha encontrado un usuario con ese email: " + email));

        String roleName = user.getRole() != null ? user.getRole().getRolName() : "NORMUSER"; // Rol predeterminado si es null

        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(roleName)));
    }

    public UserDto login(@NonNull CredentialsDto credentialsDto) {
        User user = userRepository.findByEmail(credentialsDto.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("No se ha encontrado un usuario con ese email: " + credentialsDto.getEmail()));

        // Compara la contraseña proporcionada con la almacenada
        if (!passwordEncoder.matches(credentialsDto.getPassword(), user.getPassword())) {
            throw new UsernameNotFoundException("Contraseña incorrecta");
        }

        // Verifica si el usuario está verificado
        if (!user.getUserVerified()) {
            throw new IllegalArgumentException("El usuario no está verificado, por favor revise su correo electrónico");
        }

        // Obtiene el nombre del rol del usuario
        String roleName = user.getRole() != null ? user.getRole().getRolName() : "NORMUSER"; // Rol predeterminado si es null

        // Log con toda la información del usuario logueado.
        logger.info("Usuario logueado con éxito: {}", user);

        // Devuelve el UserDto
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .firstSurname(user.getFirstSurname())
                .secondSurname(user.getSecondSurname())
                .idCard(user.getIdCard())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .idImage(imageMapper.toImageDto(user.getIdImage())) // Usa el método de ImageMapper
                .role(roleMapper.toRoleDto(user.getRole())) // Usa el método de RoleMapper
                .userVerified(user.getUserVerified())
                .status(user.getStatus())
                .build();
    }

    @Transactional // Asegura que la transacción se complete o se revierta si se produce una excepción
    public UserDto register(@NonNull SignUpDto signUpDto) {

        logger.info("Iniciando el registro del usuario: {}", signUpDto);

        // Verifica que los IDs de imagen y rol no sean nulos
        if (signUpDto.getIdImage() == null || signUpDto.getIdRol() == null) {
            logger.error("Los IDs de imagen y rol no pueden ser nulos: idImage={}, idRol={}", signUpDto.getIdImage(), signUpDto.getIdRol());
            throw new IllegalArgumentException("Los IDs de imagen y rol no pueden ser nulos");
        }

        // Encuentra imagen y rol asociados
        Image image = imageRepository.findById(signUpDto.getIdImage())
                .orElseThrow(() -> {
                    logger.error("La imagen no se ha encontrado: idImage={}", signUpDto.getIdImage());
                    return new RuntimeException("La imagen no se ha encontrado");
                });

        Rol rol = rolRepository.findById(signUpDto.getIdRol())
                .orElseThrow(() -> {
                    logger.error("El rol no se ha encontrado: idRol={}", signUpDto.getIdRol());
                    return new RuntimeException("El rol no se ha encontrado");
                });

        // Crea nuevo usuario utilizando el UserMapper
        User newUser = User.builder()
                .name(signUpDto.getName())
                .firstSurname(signUpDto.getFirstSurname())
                .secondSurname(signUpDto.getSecondSurname())
                .idCard(signUpDto.getIdCard())
                .phoneNumber(signUpDto.getPhoneNumber())
                .idImage(image) // Asignación directa de la imagen
                .role(rol) // Asignación directa del rol
                .email(signUpDto.getEmail())
                .password(passwordEncoder.encode(signUpDto.getPassword())) // Codificación de la contraseña
                .userVerified(false) // Usuario no verificado inicialmente
                .status(signUpDto.getStatus())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        logger.info("Nuevo usuario creado: {}", newUser);

        // Validar campos únicos mediante validateUniqueFields.
        validateUniqueFields(signUpDto);

        // Guarda el usuario en la base de datos
        User savedUser = userRepository.save(newUser);
        logger.info("Usuario guardado en la base de datos: {}", savedUser);

        // Genera y envía el correo de activación
        String activationToken = tokenService.generateActivationToken();
        Instant tokenExpiration = tokenService.getTokenExpiration();
        savedUser.setActivationToken(activationToken);
        savedUser.setActivationTokenExpiration(tokenExpiration);

        userRepository.save(savedUser); // Asegúrate de que el usuario se guarda nuevamente después de establecer el token

        // Enviar el correo de activación
        String activationLink = "http://localhost:8080/auth/activate?token=" + activationToken;
        emailService.sendActivationEmail(savedUser.getEmail(), activationLink);
        logger.info("Correo de activación enviado a: {}", savedUser.getEmail());

        // Log con toda la información del usuario registrado
        logger.info("Usuario registrado: {}", savedUser);

        // Devuelve el UserDto, mapeando las entidades a sus DTOs
        UserDto userDto = UserDto.builder()
                .id(savedUser.getId())
                .name(savedUser.getName())
                .firstSurname(savedUser.getFirstSurname())
                .secondSurname(savedUser.getSecondSurname())
                .idCard(savedUser.getIdCard())
                .phoneNumber(savedUser.getPhoneNumber())
                .email(savedUser.getEmail())
                .idImage(imageMapper.toImageDto(savedUser.getIdImage())) // Usa el método de ImageMapper
                .role(roleMapper.toRoleDto(savedUser.getRole())) // Usa el método de RoleMapper
                .userVerified(savedUser.getUserVerified())
                .status(savedUser.getStatus())
                .createdAt(savedUser.getCreatedAt())
                .updatedAt(savedUser.getUpdatedAt())
                .build();

        logger.info("UserDto devuelto: {}", userDto);
        return userDto;
    }

    public UserDto findByEmail(@NonNull String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No se ha encontrado un usuario con ese email: " + email));
        // Devuelve el UserDto
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .firstSurname(user.getFirstSurname())
                .secondSurname(user.getSecondSurname())
                .idCard(user.getIdCard())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .idImage(imageMapper.toImageDto(user.getIdImage())) // Usa el método de ImageMapper
                .role(roleMapper.toRoleDto(user.getRole())) // Usa el método de RoleMapper
                .userVerified(user.getUserVerified())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public boolean activateUser(String token) {
        User user = userRepository.findByActivationToken(token);
        if (user != null && user.getActivationTokenExpiration().isAfter(Instant.now())) {
            user.setUserVerified(true);
            user.setActivationToken(null);
            user.setActivationTokenExpiration(null);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    //Método para actualizar la contraseña de un usuario
    public boolean updatePassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No se ha encontrado un usuario con ese email: " + email));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }

    public List<UserDto> findAll() {
        //Log con toda la información de los usuarios obtenidos.
        logger.info("Usuarios obtenidos: {}", userRepository.findAll());
        return userRepository.findAll().stream()
                .map(this::toUserDto)
                .collect(Collectors.toList());
    }

    public UserDto findById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se ha encontrado un usuario con ese ID: " + id));
        return toUserDto(user);
    }

    public UserDto update(Integer id, SignUpDto updatedUserDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se ha encontrado un usuario con ese ID: " + id));

        // Verifica si el correo electrónico ya está en uso
        if (!user.getEmail().equals(updatedUserDto.getEmail()) && userRepository.existsByEmail(updatedUserDto.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese correo electrónico asignado: " + updatedUserDto.getEmail());
        }

        // Verifica si el numero de cedula ya está en uso
        if (!user.getIdCard().equals(updatedUserDto.getIdCard()) && userRepository.existsByIdCard(updatedUserDto.getIdCard())) {
            throw new IllegalArgumentException("Ese número de cédula ya se encuentra en uso: " + updatedUserDto.getIdCard());
        }

        // Verifica si el numero telefonico ya está en uso
        if (!user.getPhoneNumber().equals(updatedUserDto.getPhoneNumber()) && userRepository.existsByPhoneNumber(updatedUserDto.getPhoneNumber())) {
            throw new IllegalArgumentException("El número de teléfono ya se encuentra en uso: " + updatedUserDto.getPhoneNumber());
        }

        // Actualiza los campos del usuario
        user.setName(updatedUserDto.getName());
        user.setFirstSurname(updatedUserDto.getFirstSurname());
        user.setSecondSurname(updatedUserDto.getSecondSurname());
        user.setIdCard(updatedUserDto.getIdCard());
        user.setPhoneNumber(updatedUserDto.getPhoneNumber());
        user.setEmail(updatedUserDto.getEmail());
        user.setPassword(passwordEncoder.encode(updatedUserDto.getPassword()));
        user.setUpdatedAt(Instant.now());

        userRepository.save(user);

        return toUserDto(user);
    }

    public void delete(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se ha encontrado un usuario con ese ID: " + id));
        userRepository.delete(user);
    }

    private UserDto toUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .firstSurname(user.getFirstSurname())
                .secondSurname(user.getSecondSurname())
                .idCard(user.getIdCard())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .idImage(imageMapper.toImageDto(user.getIdImage())) // Usa ImageMapper para convertir la imagen
                .role(roleMapper.toRoleDto(user.getRole())) // Usa el método de RoleMapper
                .userVerified(user.getUserVerified())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    //Metodo para validar campos unicos en los registros de usuario.
    public void validateUniqueFields(SignUpDto signUpDto) {
        // Verifica si el correo electrónico ya está en uso
        if (userRepository.findByEmail(signUpDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un usuario con ese correo electrónico asignado: " + signUpDto.getEmail());
        }

        // Verifica si el idCard ya está en uso
        if (userRepository.existsByIdCard(signUpDto.getIdCard())) {
            throw new IllegalArgumentException("Ese número de cédula ya se encuentra en uso: " + signUpDto.getIdCard());
        }

        // Verifica si el phoneNumber ya está en uso
        if (userRepository.existsByPhoneNumber(signUpDto.getPhoneNumber())) {
            throw new IllegalArgumentException("El número de teléfono ya se encuentra en uso: " + signUpDto.getPhoneNumber());
        }
    }

}
