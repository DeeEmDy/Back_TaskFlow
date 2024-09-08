package com.taskflow.backend.services;

import com.taskflow.backend.dto.CredentialsDto;
import com.taskflow.backend.dto.SignUpDto;
import com.taskflow.backend.dto.UserDto;
import com.taskflow.backend.entities.User;
import com.taskflow.backend.exception.AppException;
import com.taskflow.backend.mappers.UserMapper;
import com.taskflow.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.CharBuffer;
import java.time.Instant;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder; //Para poder hashear la contraseña y que se almacene en base de datos de manera inleible.

    public UserDto findByLogin(String email){

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException("Usuario desconocido!", HttpStatus.NOT_FOUND));

        return  userMapper.toUserDto(user); //Mediante el mapper convertir a nuestro usuario en un objeto DTO para convertir el JSON.
    }

    public UserDto login(CredentialsDto credentialsDto) {
            User user = userRepository.findByEmail(credentialsDto.getEmail())
                    .orElseThrow(() -> new AppException("Usuario desconocido!", HttpStatus.NOT_FOUND));

            if(passwordEncoder.matches(CharBuffer.wrap(credentialsDto.getPassword()), user.getPassword())) {
                return  userMapper.toUserDto(user);
            }

            throw new AppException("Contraseña Inválida", HttpStatus.BAD_REQUEST);
    }


    //-----------------------------Registro de usuario--------------------------//
    public UserDto register(SignUpDto signUpDto) {
        // Verificar si el usuario ya existe
        Optional<User> optionalUser = userRepository.findByEmail(signUpDto.getEmail());

        if (optionalUser.isPresent()) {
            throw new AppException("¡Ese usuario ya existe!", HttpStatus.BAD_REQUEST);
        }

        // Convertir el SignUpDto a User usando el mapper
        User user = userMapper.signUpToUser(signUpDto);

        // Establecer campos adicionales
        user.setPassword(passwordEncoder.encode(signUpDto.getPassword())); // Codificar la contraseña
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        user.setStatus(true); // Establecer el estado como activo

        // Verificar y establecer objetos Image y Rol si no son null
        if (signUpDto.getId_image() != null) {
            user.setIdImage(signUpDto.getId_image());
        }
        if (signUpDto.getId_rol() != null) {
            user.setIdRol(signUpDto.getId_rol());
        }

        // Guardar el usuario en la base de datos
        User savedUser = userRepository.save(user);

        // Convertir el usuario guardado a DTO y devolverlo
        return userMapper.toUserDto(savedUser);
    }

}
