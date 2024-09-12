package com.taskflow.backend.controllers;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.taskflow.backend.config.UserAuthProvider;
import com.taskflow.backend.dto.AuthResponseDto;
import com.taskflow.backend.dto.CredentialsDto;
import com.taskflow.backend.dto.SignUpDto;
import com.taskflow.backend.dto.UserDto;
import com.taskflow.backend.services.UserService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
public class AuthController {

    private final UserService userService;
    private final UserAuthProvider userAuthProvider;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody CredentialsDto credentialsDto) {
        try {
            UserDto user = userService.login(credentialsDto);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null); // Respond without body
            }
            String token = userAuthProvider.createToken(user.getEmail());
            AuthResponseDto response = AuthResponseDto.builder()
                    .token(token)
                    .user(user)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Respond without body
        }
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@RequestBody SignUpDto signUpDto) {
        System.out.println("Registering user: " + signUpDto.getEmail());
        try {
            UserDto user = userService.register(signUpDto);
            System.out.println("User registered successfully: " + user.getEmail());
            return ResponseEntity.created(URI.create("/users/" + user.getId())).body(user);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Respond without body
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponseDto> refreshToken(@RequestBody String refreshToken) {
        try {
            Authentication authentication = userAuthProvider.validateToken(refreshToken);
            UserDto user = (UserDto) authentication.getPrincipal();
            String newToken = userAuthProvider.createToken(user.getEmail());

            AuthResponseDto response = AuthResponseDto.builder()
                    .token(newToken)
                    .user(user)
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null); // Respond without body
        }
    }
}