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
import com.taskflow.backend.repositories.ImageRepository;
import com.taskflow.backend.repositories.RolRepository;
import com.taskflow.backend.repositories.UserRepository; // Ajuste aquí

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RolRepository rolRepository; // Ajuste aquí
    private final ImageRepository imageRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final TokenService tokenService; // Ajuste aquí
    private final EmailService emailService; // Ajuste aquí

    @Override
    public UserDetails loadUserByUsername(@SuppressWarnings("null") @NonNull String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        String roleName = user.getRole() != null ? user.getRole().getRolName() : "USER"; // Default role if null

        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(roleName)));
    }

    public UserDto login(@NonNull CredentialsDto credentialsDto) {
        User user = userRepository.findByEmail(credentialsDto.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + credentialsDto.getEmail()));

        // Compare the provided password with the stored password
        if (!passwordEncoder.matches(credentialsDto.getPassword(), user.getPassword())) {
            throw new UsernameNotFoundException("Invalid password");
        }

        String roleName = user.getRole() != null ? user.getRole().getRolName() : "USER"; // Default role if null

        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .firstSurname(user.getFirstSurname())
                .secondSurname(user.getSecondSurname())
                .idCard(user.getIdCard())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .idImage(user.getIdImage() != null ? user.getIdImage().getId() : null)
                .roles(Collections.singletonList(roleName)) // Wrap role name in a list
                .userVerified(user.getUserVerified())
                .status(user.getStatus())
                .build();
    }

    // Cambia la firma del método y el tipo de retorno de UserDto a User
    public User register(@NonNull SignUpDto signUpDto) {
        // Verificar si el correo electrónico ya está en uso
        if (userRepository.findByEmail(signUpDto.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Ya existe un usuario con ese correo electrónico asignado:" + signUpDto.getEmail());
        }

        // Verificar si el idCard ya está en uso
        if (userRepository.existsByIdCard(signUpDto.getIdCard())) {
            throw new IllegalArgumentException("Ese número de cédula ya se encuentra en uso: " + signUpDto.getIdCard());
        }

        // Verificar si el phoneNumber ya está en uso
        if (userRepository.existsByPhoneNumber(signUpDto.getPhoneNumber())) {
            throw new IllegalArgumentException("El numero de télefono ya se encuentra en uso: " + signUpDto.getPhoneNumber());
        }

        // Verificar que los IDs de imagen y rol no sean nulos
        if (signUpDto.getIdImage() == null || signUpDto.getIdRol() == null) {
            throw new IllegalArgumentException("Image ID and Role ID must not be null");
        }

        // Encontrar imagen y rol asociados
        Image image = imageRepository.findById(signUpDto.getIdImage())
                .orElseThrow(() -> new RuntimeException("Image not found"));
        Rol rol = rolRepository.findById(signUpDto.getIdRol())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        // Crear nuevo usuario
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
                .userVerified(false) // El usuario no está verificado inicialmente
                .status(signUpDto.getStatus())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

// Guardar el usuario en la base de datos
        User savedUser = userRepository.save(newUser);

// Generar y enviar el correo de activación
        String activationToken = tokenService.generateActivationToken();
        Instant tokenExpiration = tokenService.getTokenExpiration();

        savedUser.setActivationToken(activationToken); // Asegúrate de que el método existe
        savedUser.setActivationTokenExpiration(tokenExpiration); // Asegúrate de que el método existe

        userRepository.save(savedUser);

        String activationLink = "http://localhost:8080/auth/activate?token=" + activationToken;
        emailService.sendActivationEmail(savedUser.getEmail(), activationLink);

        return savedUser;
    }

    public UserDto findByEmail(@NonNull String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        String roleName = user.getRole() != null ? user.getRole().getRolName() : "NORMUSER"; // Default role if null

        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .firstSurname(user.getFirstSurname())
                .secondSurname(user.getSecondSurname())
                .idCard(user.getIdCard())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .idImage(user.getIdImage() != null ? user.getIdImage().getId() : null)
                .roles(Collections.singletonList(roleName)) // Wrap role name in a list
                .userVerified(user.getUserVerified())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public boolean activateUser(String token) {
        User user = userRepository.findByActivationToken(token); // Asegúrate de que el método existe
        if (user != null && user.getActivationTokenExpiration().isAfter(Instant.now())) {
            user.setUserVerified(true);
            user.setActivationToken(null); // Asegúrate de que el método existe
            user.setActivationTokenExpiration(null); // Asegúrate de que el método existe
            userRepository.save(user);
            return true;
        }
        return false;
    }

}
