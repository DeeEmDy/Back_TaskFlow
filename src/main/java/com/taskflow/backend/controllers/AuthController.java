package com.taskflow.backend.controllers;

import com.taskflow.backend.config.UserAuthProvider;
import com.taskflow.backend.dto.CredentialsDto;
import com.taskflow.backend.dto.SignUpDto;
import com.taskflow.backend.dto.UserDto;
import com.taskflow.backend.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<?> login(@RequestBody CredentialsDto credentialsDto) {
        try {
            UserDto user = userService.login(credentialsDto);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales inválidas");
            }
            user.setToken(userAuthProvider.createToken(user.getEmail()));
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error en el servidor");
        }
    }


    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody SignUpDto signUpDto) {
        try {
            UserDto user = userService.register(signUpDto);
            user.setToken(userAuthProvider.createToken(user.getEmail()));
            return ResponseEntity.created(URI.create("/users/" + user.getId())).body(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error en el servidor");
        }
    }
}
