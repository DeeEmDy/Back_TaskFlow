package com.taskflow.backend.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
@RequestMapping("/auth") // Prefijo común para todos los endpoints de este controlador.
public class AuthController {

    private final UserService userService;
    private final UserAuthProvider userAuthProvider;

    // Endpoint para login
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody CredentialsDto credentialsDto) {
        // Lógica para autenticar el usuario y devolver el token
        UserDto user = userService.login(credentialsDto);
        String token = userAuthProvider.createToken(user.getEmail());

        return ResponseEntity.ok(AuthResponseDto.builder()
                .token(token)
                .user(user)
                .build());
    }
    // Endpoint para registrar un nuevo usuario
    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@RequestBody SignUpDto signUpDto) {
        // Lógica para registrar un usuario
        UserDto newUser = userService.register(signUpDto);
        return ResponseEntity.ok(newUser);
    }

    // Endpoint para refrescar el token de autenticación
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

    // Endpoint para obtener información del usuario autenticado
    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(Authentication authentication) {
        String email = authentication.getName(); // El email del usuario autenticado
        UserDto user = userService.findByEmail(email);
        return ResponseEntity.ok(user);
    }
}
