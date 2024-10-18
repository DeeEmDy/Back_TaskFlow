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
import com.taskflow.backend.enums.RoleTypeEnum;
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
                .orElseThrow(() -> new UsernameNotFoundException("No se ha encontrado un usuario con ese email: " + credentialsDto.getEmail()));

        if (!passwordEncoder.matches(credentialsDto.getPassword(), user.getPassword())) {
            throw new UsernameNotFoundException("Contraseña incorrecta");
        }

        if (!user.getUserVerified()) {
            throw new IllegalArgumentException("El usuario no está verificado, por favor revise su correo electrónico");
        }

        logger.info("Usuario logueado con éxito: {}", user);

        return mapToUserDto(user);
    }

    @Transactional
    public UserDto register(@NonNull SignUpDto signUpDto) {
        logger.info("Iniciando el registro del usuario: {}", signUpDto);

        if (signUpDto.getIdImage() == null || signUpDto.getIdRol() == null) {
            logger.error("Los IDs de imagen y rol no pueden ser nulos: idImage={}, idRol={}", signUpDto.getIdImage(), signUpDto.getIdRol());
            throw new IllegalArgumentException("Los IDs de imagen y rol no pueden ser nulos");
        }

        Image image = imageRepository.findById(signUpDto.getIdImage())
                .orElseThrow(() -> new RuntimeException("La imagen no se ha encontrado"));

        //Para el registro de un usuario, se asigna el rol ROLE_NORMUSER por defecto.
        Rol rolNormUser = rolRepository.findByRolName(RoleTypeEnum.ROLE_NORMUSER.name())
                .orElseThrow(() -> new RuntimeException("El rol ROLE_NORMUSER no se ha encontrado"));

        User newUser = User.builder()
                .name(signUpDto.getName())
                .firstSurname(signUpDto.getFirstSurname())
                .secondSurname(signUpDto.getSecondSurname())
                .idCard(signUpDto.getIdCard())
                .phoneNumber(signUpDto.getPhoneNumber())
                .idImage(image)
                .role(rolNormUser)
                .email(signUpDto.getEmail())
                .password(passwordEncoder.encode(signUpDto.getPassword()))
                .userVerified(false)
                .status(signUpDto.getStatus())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        logger.info("Nuevo usuario creado: {}", newUser);
        validateUniqueFields(signUpDto);

        User savedUser = userRepository.save(newUser);
        logger.info("Usuario guardado en la base de datos: {}", savedUser);

        String activationToken = tokenService.generateActivationToken();
        savedUser.setActivationToken(activationToken);
        savedUser.setActivationTokenExpiration(tokenService.getTokenExpiration());

        userRepository.save(savedUser);
        String activationLink = "http://localhost:8080/auth/activate?token=" + activationToken;
        emailService.sendActivationEmail(savedUser.getEmail(), activationLink);
        logger.info("Correo de activación enviado a: {}", savedUser.getEmail());

        return mapToUserDto(savedUser);
    }

    public UserDto findByEmail(@NonNull String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No se ha encontrado un usuario con ese email: " + email));
        return mapToUserDto(user);
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

    public boolean updatePassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No se ha encontrado un usuario con ese email: " + email));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
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

    public UserDto update(Integer id, SignUpDto updatedUserDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se ha encontrado un usuario con ese ID: " + id));

        // Verificación de campos únicos
        validateUniqueFieldsForUpdate(user, updatedUserDto);

        // Actualización de campos del usuario
        user.setName(updatedUserDto.getName());
        user.setFirstSurname(updatedUserDto.getFirstSurname());
        user.setSecondSurname(updatedUserDto.getSecondSurname());
        user.setIdCard(updatedUserDto.getIdCard());
        user.setPhoneNumber(updatedUserDto.getPhoneNumber());
        user.setEmail(updatedUserDto.getEmail());

        // Cambia aquí para usar idRol
        Rol rol = rolRepository.findById(updatedUserDto.getIdRol())
                .orElseThrow(() -> new RuntimeException("El rol no se ha encontrado"));
        user.setRole(rol); // Asignar el rol actualizado

        user.setStatus(updatedUserDto.getStatus());
        user.setUpdatedAt(Instant.now());

        userRepository.save(user);
        logger.info("Usuario actualizado: {}", user);

        return mapToUserDto(user);
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

    private void validateUniqueFieldsForUpdate(User user, SignUpDto updatedUserDto) {
        // Verificaciones para campos únicos
        if (!user.getEmail().equals(updatedUserDto.getEmail()) && userRepository.existsByEmail(updatedUserDto.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese correo electrónico asignado: " + updatedUserDto.getEmail());
        }
        if (!user.getIdCard().equals(updatedUserDto.getIdCard()) && userRepository.existsByIdCard(updatedUserDto.getIdCard())) {
            throw new IllegalArgumentException("Ya existe un usuario con esa cédula asignada: " + updatedUserDto.getIdCard());
        }
    }

    private void validateUniqueFields(SignUpDto signUpDto) {
        if (userRepository.existsByEmail(signUpDto.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese correo electrónico: " + signUpDto.getEmail());
        }
        if (userRepository.existsByIdCard(signUpDto.getIdCard())) {
            throw new IllegalArgumentException("Ya existe un usuario con esa cédula: " + signUpDto.getIdCard());
        }
    }

    //Método para eliminar un registro de usuario.
    public void delete(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se ha encontrado un usuario con ese ID: " + id));
        userRepository.delete(user);
        logger.info("Usuario eliminado: {}", user);
    }

    //Método para crear un registro de usuario, en este se puede asignar el ROL del usuario para así poder crear ADMINS.
    @Transactional
    public UserDto createUser(@NonNull SignUpDto signUpDto) {
        if (signUpDto.getIdImage() == null) {
            throw new IllegalArgumentException("El ID de la imagen no puede ser nulo");
        }

        Image image = imageRepository.findById(signUpDto.getIdImage())
                .orElseThrow(() -> new RuntimeException("La imagen no se ha encontrado"));

        // Buscar el rol
        Rol rol = rolRepository.findById(signUpDto.getIdRol())
                .orElseThrow(() -> new RuntimeException("El rol no se ha encontrado"));

        // Crear el nuevo usuario con el rol especificado
        User newUser = User.builder()
                .name(signUpDto.getName())
                .firstSurname(signUpDto.getFirstSurname())
                .secondSurname(signUpDto.getSecondSurname())
                .idCard(signUpDto.getIdCard())
                .phoneNumber(signUpDto.getPhoneNumber())
                .idImage(image)
                .role(rol) // Asignar el rol especificado
                .email(signUpDto.getEmail())
                .password(passwordEncoder.encode(signUpDto.getPassword()))
                .userVerified(true) //El usuario creado por un Administrador, ya está verificado por defecto.
                .status(signUpDto.getStatus())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        logger.info("Nuevo usuario creado: {}", newUser);

        // Validar campos únicos
        validateUniqueFields(signUpDto);

        // Guardar el usuario
        User savedUser = userRepository.save(newUser);
        logger.info("Usuario guardado en la base de datos: {}", savedUser);

        // Enviar email de activación
        String activationToken = tokenService.generateActivationToken();
        savedUser.setActivationToken(activationToken);
        savedUser.setActivationTokenExpiration(tokenService.getTokenExpiration());

        userRepository.save(savedUser);
        String activationLink = "http://localhost:8080/auth/activate?token=" + activationToken;
        emailService.sendActivationEmail(savedUser.getEmail(), activationLink);
        logger.info("Correo de activación enviado a: {}", savedUser.getEmail());

        return mapToUserDto(savedUser);
    }
}
