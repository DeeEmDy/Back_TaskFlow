package com.taskflow.backend.services;

import java.time.Instant;
import java.util.Collections;

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
import com.taskflow.backend.entities.User; // Ajuste aquí
import com.taskflow.backend.exception.UserAlreadyExistsException;
import com.taskflow.backend.mappers.ImageMapper;
import com.taskflow.backend.mappers.RoleMapper;
import com.taskflow.backend.repositories.ImageRepository;
import com.taskflow.backend.repositories.RolRepository;
import com.taskflow.backend.repositories.UserRepository; // Ajuste aquí

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

    @Override
    public UserDetails loadUserByUsername(@SuppressWarnings("null") @NonNull String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        String roleName = user.getRole() != null ? user.getRole().getRolName() : "USER"; // Rol predeterminado si es null

        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(roleName)));
    }

    public UserDto login(@NonNull CredentialsDto credentialsDto) {
        User user = userRepository.findByEmail(credentialsDto.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + credentialsDto.getEmail()));

        // Compara la contraseña proporcionada con la almacenada
        if (!passwordEncoder.matches(credentialsDto.getPassword(), user.getPassword())) {
            throw new UsernameNotFoundException("Invalid password");
        }

        String roleName = user.getRole() != null ? user.getRole().getRolName() : "USER"; // Rol predeterminado si es null

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
                .roles(Collections.singletonList(roleName))
                .userVerified(user.getUserVerified())
                .status(user.getStatus())
                .build();
    }

    public UserDto register(@NonNull SignUpDto signUpDto) {
        // Verifica si el correo electrónico ya está en uso
        if (userRepository.findByEmail(signUpDto.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Ya existe un usuario con ese correo electrónico asignado: " + signUpDto.getEmail());
        }

        // Verifica si el idCard ya está en uso
        if (userRepository.existsByIdCard(signUpDto.getIdCard())) {
            throw new IllegalArgumentException("Ese número de cédula ya se encuentra en uso: " + signUpDto.getIdCard());
        }

        // Verifica si el phoneNumber ya está en uso
        if (userRepository.existsByPhoneNumber(signUpDto.getPhoneNumber())) {
            throw new IllegalArgumentException("El número de teléfono ya se encuentra en uso: " + signUpDto.getPhoneNumber());
        }

        // Verifica que los IDs de imagen y rol no sean nulos
        if (signUpDto.getIdImage() == null || signUpDto.getIdRol() == null) {
            throw new IllegalArgumentException("Image ID and Role ID must not be null");
        }

        // Encuentra imagen y rol asociados
        Image image = imageRepository.findById(signUpDto.getIdImage())
                .orElseThrow(() -> new RuntimeException("Image not found"));
        Rol rol = rolRepository.findById(signUpDto.getIdRol())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        // Crea nuevo usuario
        User newUser = User.builder()
                .name(signUpDto.getName())
                .firstSurname(signUpDto.getFirstSurname())
                .secondSurname(signUpDto.getSecondSurname())
                .idCard(signUpDto.getIdCard())
                .phoneNumber(signUpDto.getPhoneNumber())
                .idImage(image)
                .role(rol)
                .email(signUpDto.getEmail())
                .password(passwordEncoder.encode(signUpDto.getPassword()))
                .userVerified(false) // Usuario no verificado inicialmente
                .status(signUpDto.getStatus())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        // Guarda el usuario en la base de datos
        User savedUser = userRepository.save(newUser);

        // Genera y envía el correo de activación
        String activationToken = tokenService.generateActivationToken();
        Instant tokenExpiration = tokenService.getTokenExpiration();

        savedUser.setActivationToken(activationToken);
        savedUser.setActivationTokenExpiration(tokenExpiration);

        userRepository.save(savedUser);

        String activationLink = "http://localhost:8080/auth/activate?token=" + activationToken;
        emailService.sendActivationEmail(savedUser.getEmail(), activationLink);

        // Devuelve el UserDto, mapeando las entidades a sus DTOs
        return UserDto.builder()
                .id(savedUser.getId())
                .name(savedUser.getName())
                .firstSurname(savedUser.getFirstSurname())
                .secondSurname(savedUser.getSecondSurname())
                .idCard(savedUser.getIdCard())
                .phoneNumber(savedUser.getPhoneNumber())
                .email(savedUser.getEmail())
                .idImage(imageMapper.toImageDto(savedUser.getIdImage())) // Usa el método de ImageMapper
                .roles(Collections.singletonList(roleMapper.toRoleDto(savedUser.getRole()).getRolName())) // Usa el método de RoleMapper
                .userVerified(savedUser.getUserVerified())
                .status(savedUser.getStatus())
                .createdAt(savedUser.getCreatedAt())
                .updatedAt(savedUser.getUpdatedAt())
                .build();
    }

    public UserDto findByEmail(@NonNull String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        String roleName = user.getRole() != null ? user.getRole().getRolName() : "USER"; // Rol predeterminado si es null

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
                .roles(Collections.singletonList(roleMapper.toRoleDto(user.getRole()).getRolName())) // Usa el método de RoleMapper
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
}
