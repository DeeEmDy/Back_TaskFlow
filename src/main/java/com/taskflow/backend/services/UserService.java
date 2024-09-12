package com.taskflow.backend.services;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

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
import com.taskflow.backend.entities.Rol; // Ajuste aquí
import com.taskflow.backend.entities.User;
import com.taskflow.backend.exception.UserAlreadyExistsException;
import com.taskflow.backend.repositories.ImageRepository;
import com.taskflow.backend.repositories.RolRepository; // Ajuste aquí
import com.taskflow.backend.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RolRepository rolRepository; // Ajuste aquí
    private final ImageRepository imageRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {
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
        Optional<User> existingUser = userRepository.findByEmail(signUpDto.getEmail());

        if (existingUser.isPresent()) {
            throw new UserAlreadyExistsException("User already exists with email: " + signUpDto.getEmail());
        }

        // Buscar imagen y rol por ID
        Image image = imageRepository.findById(signUpDto.getIdImage())
                .orElseThrow(() -> new RuntimeException("Image not found"));
        Rol rol = rolRepository.findById(signUpDto.getIdRol())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        User newUser = new User();
        newUser.setName(signUpDto.getName());
        newUser.setFirstSurname(signUpDto.getFirstSurname());
        newUser.setSecondSurname(signUpDto.getSecondSurname());
        newUser.setIdCard(signUpDto.getIdCard());
        newUser.setPhoneNumber(signUpDto.getPhoneNumber());
        newUser.setIdImage(image);
        newUser.setRole(rol);
        newUser.setEmail(signUpDto.getEmail());
        newUser.setPassword(passwordEncoder.encode(signUpDto.getPassword())); // Hash the password before saving it
        newUser.setUserVerified(signUpDto.isUserVerified());
        newUser.setStatus(signUpDto.isStatus());
        newUser.setCreatedAt(Instant.now());
        newUser.setUpdatedAt(Instant.now());

        userRepository.save(newUser);

        return UserDto.builder()
                .id(newUser.getId())
                .name(newUser.getName())
                .firstSurname(newUser.getFirstSurname())
                .secondSurname(newUser.getSecondSurname())
                .idCard(newUser.getIdCard())
                .phoneNumber(newUser.getPhoneNumber())
                .email(newUser.getEmail())
                .idImage(newUser.getIdImage().getId()) // ID de la imagen
                .roles(Collections.singletonList(newUser.getRole().getRolName())) // Nombre del rol
                .userVerified(newUser.getUserVerified())
                .status(newUser.getStatus())
                .createdAt(newUser.getCreatedAt())
                .updatedAt(newUser.getUpdatedAt())
                .build();
    }

    public UserDto findByEmail(@NonNull String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

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
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
