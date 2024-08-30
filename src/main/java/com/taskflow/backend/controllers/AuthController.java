package com.taskflow.backend.controllers;

import com.taskflow.backend.config.UserAuthProvider;
import com.taskflow.backend.dto.CredentialsDto;
import com.taskflow.backend.dto.SignUpDto;
import com.taskflow.backend.dto.UserDto;
import com.taskflow.backend.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RequiredArgsConstructor
@RestController
public class AuthController {

    private final UserService userService;
    private final UserAuthProvider userAuthProvider;

    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@RequestBody CredentialsDto credentialsDto) {

        UserDto user = userService.login(credentialsDto);


        user.setToken(userAuthProvider.createToken(user.getEmail()));

        return ResponseEntity.ok(user);
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@RequestBody SignUpDto signUpDto) {

        UserDto user = userService.register(signUpDto);
        user.setToken(userAuthProvider.createToken(user.getEmail()));
        return ResponseEntity.created(URI.create("/users/" + user.getId())).body(user); //Devolver codigo de exito 201 + url donde se desea redirigir.
    }

}
