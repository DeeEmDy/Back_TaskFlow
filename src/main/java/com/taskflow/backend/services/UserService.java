package com.taskflow.backend.services;

import com.taskflow.backend.dto.CredentialsDto;
import com.taskflow.backend.dto.SignUpDto;
import com.taskflow.backend.dto.UserDto;
import com.taskflow.backend.entities.User;
import com.taskflow.backend.exception.AppException;
import com.taskflow.backend.mappers.UserMapper;
import com.taskflow.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContextException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.CharBuffer;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder; //Para poder hashear la contraseña y que se almacene en base de datos de manera inleible.

    public UserDto findByLogin(String email){

        User user = userRepository.findByLogin(email)
                .orElseThrow(() -> new AppException("Usuario desconocido!", HttpStatus.NOT_FOUND));

        return  userMapper.toUserDto(user); //Mediante el mapper convertir a nuestro usuario en un objeto DTO para convertir el JSON.
    }

    public UserDto login(CredentialsDto credentialsDto) {
            User user = userRepository.findByLogin(credentialsDto.getEmail())
                    .orElseThrow(() -> new AppException("Usuario desconocido!", HttpStatus.NOT_FOUND));

            if(passwordEncoder.matches(CharBuffer.wrap(credentialsDto.getPassword()), user.getPassword())) {
                return  userMapper.toUserDto(user);
            }

            throw new AppException("Contraseña Inválida", HttpStatus.BAD_REQUEST);
    }

    public UserDto register(SignUpDto userDto) {

        Optional<User> optionalUser = userRepository.findByLogin(userDto.getEmail());

        if(optionalUser.isPresent()) {

            throw new AppException("¡Ese usuario ya existe!", HttpStatus.BAD_REQUEST);
        }

        User user = userMapper.signUpToUser(userDto);

        user.setPassword(passwordEncoder.encode(CharBuffer.wrap(userDto.getPassword()))); //Para codificar las contraseñas - para hashearlas a nivel de BD.

        User savedUser = userRepository.save(user); //Almacenar el registro del usuario creado.


        return userMapper.toUserDto(user); //Utilizar el mapper para construir objeto JSON del usuario a través del DTO.
    }
}
