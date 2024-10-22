package com.taskflow.backend.controllers;

import java.util.Map;

import javax.management.relation.RoleNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.taskflow.backend.config.UserAuthProvider;
import com.taskflow.backend.dto.AuthResponseDto;
import com.taskflow.backend.dto.CredentialsDto;
import com.taskflow.backend.dto.SignUpDto;
import com.taskflow.backend.dto.UserDto;
import com.taskflow.backend.exception.IdCardAlreadyExistsException;
import com.taskflow.backend.exception.ImageNotFoundException;
import com.taskflow.backend.exception.JwtAuthenticationException;
import com.taskflow.backend.exception.PhoneNumberAlreadyExistsException;
import com.taskflow.backend.exception.UserAlreadyExistsException;
import com.taskflow.backend.services.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final UserService userService;
    private final UserAuthProvider userAuthProvider;

    // Endpoint para login
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody CredentialsDto credentialsDto) {
        UserDto user = userService.login(credentialsDto);
        String token = userAuthProvider.createToken(user.getEmail());

        return ResponseEntity.ok(AuthResponseDto.builder()
                .token(token)
                .user(user)
                .build());
    }

    @PostMapping("/register")
    public ResponseEntity<Object> registerUser(@Valid @RequestBody SignUpDto signUpDto) {
        try {
            // Intentar registrar al usuario.
            UserDto newUser = userService.register(signUpDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
        } catch (IdCardAlreadyExistsException | PhoneNumberAlreadyExistsException | UserAlreadyExistsException | ImageNotFoundException e) {
            logger.warn("Error en el registro: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createErrorResponse("BAD_REQUEST", e.getMessage()));
        } catch (RoleNotFoundException e) {
            logger.warn("Error en el registro: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse("NOT_FOUND", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error inesperado: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse("INTERNAL_SERVER_ERROR", "Error interno del servidor"));
        }
    }

    // Método para crear la respuesta de error
    private Map<String, String> createErrorResponse(String errorCode, String message) {
        return Map.of("error", message, "code", errorCode);
    }

    // Endpoint para refrescar el token de autenticación
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponseDto> refreshToken(@RequestBody String refreshToken) {
        try {
            Authentication authentication = userAuthProvider.validateRefreshToken(refreshToken);
            UserDto user = (UserDto) authentication.getPrincipal();
            String newToken = userAuthProvider.createToken(user.getEmail());

            return ResponseEntity.ok(AuthResponseDto.builder()
                    .token(newToken)
                    .user(user)
                    .build());
        } catch (JwtAuthenticationException e) {
            AuthResponseDto response = AuthResponseDto.builder()
                    .token(null)
                    .user(null)
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    // Endpoint para obtener información del usuario autenticado
    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        UserDto user = userService.findByEmail(email);
        return ResponseEntity.ok(user);
    }

    // Endpoint para activar la cuenta de un usuario
    @GetMapping("/activate")
    public ResponseEntity<String> activateAccount(@RequestParam("token") String token) {
        boolean activated = userService.activateUser(token);

        if (activated) {
            return ResponseEntity.ok("Account activated successfully!");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired activation token.");
        }
    }

    // Endpoint para cerrar sesión del usuario.
    @DeleteMapping("/logout")
    public ResponseEntity<String> logout(Authentication authentication) {
        if (authentication == null || authentication.getCredentials() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No se ha podido cerrar sesión. Token inválido.");
        }
        try {
            String token = authentication.getCredentials().toString();
            userAuthProvider.revokeToken(token);
            return ResponseEntity.ok("Se ha cerrado la sesión correctamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Cerrar sesión fallida");
        }
    }

}
