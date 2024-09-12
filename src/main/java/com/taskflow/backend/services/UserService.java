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

    public UserDto register(@NonNull SignUpDto signUpDto) {
        // Verificar si el usuario ya existe
        if (userRepository.findByEmail(signUpDto.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("User already exists with email: " + signUpDto.getEmail());
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
                .userVerified(signUpDto.getUserVerified())
                .status(signUpDto.getStatus())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    
        // Guardar el usuario en la base de datos
        User savedUser = userRepository.save(newUser);
    
        // Devolver el DTO del usuario registrado
        return UserDto.builder()
                .id(savedUser.getId())
                .name(savedUser.getName())
                .firstSurname(savedUser.getFirstSurname())
                .secondSurname(savedUser.getSecondSurname())
                .idCard(savedUser.getIdCard())
                .phoneNumber(savedUser.getPhoneNumber())
                .idImage(savedUser.getIdImage().getId())
                .role(savedUser.getRole().getRolName())
                .email(savedUser.getEmail())
                .userVerified(savedUser.getUserVerified())
                .status(savedUser.getStatus())
                .createdAt(savedUser.getCreatedAt())
                .updatedAt(savedUser.getUpdatedAt())
                .build();
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
}
