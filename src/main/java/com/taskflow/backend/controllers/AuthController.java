package com.taskflow.backend.controllers;

import java.util.Map;
import javax.management.relation.RoleNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import com.taskflow.backend.config.UserAuthProvider;
import com.taskflow.backend.dto.ApiError;
import com.taskflow.backend.dto.ApiResponse;
import com.taskflow.backend.dto.AuthResponseDto;
import com.taskflow.backend.dto.CredentialsDto;
import com.taskflow.backend.dto.SignUpDto;
import com.taskflow.backend.dto.UserDto;
import com.taskflow.backend.exception.*;
import com.taskflow.backend.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final UserService userService;
    private final UserAuthProvider userAuthProvider;

    // Endpoint para login
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDto>> login(@RequestBody CredentialsDto credentialsDto) {
        try {
            UserDto user = userService.login(credentialsDto);
            String token = userAuthProvider.createToken(user.getEmail());
            AuthResponseDto response = AuthResponseDto.builder()
                    .token(token)
                    .user(user)
                    .build();
            return ResponseEntity.ok(ApiResponse.success(response, "Login exitoso."));
        } catch (EmailNotFoundException | InvalidCredentialsException e) {
            logger.warn("Error en login: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(new ApiError("UNAUTHORIZED", e.getMessage(), null)));
        } // Nuevo catch para credenciales inválidas
        catch (IllegalArgumentException e) {
            logger.warn("Error en login: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(new ApiError("FORBIDDEN", e.getMessage(), null)));
        } catch (Exception e) {
            logger.error("Error inesperado en login: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(new ApiError("INTERNAL_SERVER_ERROR", "Error interno del servidor", null)));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Object>> registerUser(@Valid @RequestBody SignUpDto signUpDto) {
        try {
            UserDto newUser = userService.register(signUpDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(newUser, "Usuario registrado con éxito."));
        } catch (IdCardAlreadyExistsException | PhoneNumberAlreadyExistsException | UserAlreadyExistsException | ImageNotFoundException e) {
            logger.warn("Error en el registro: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(new ApiError("BAD_REQUEST", e.getMessage(), null)));
        } catch (RoleNotFoundException e) {
            logger.warn("Error en el registro: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(new ApiError("NOT_FOUND", e.getMessage(), null)));
        } catch (Exception e) {
            logger.error("Error inesperado: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(new ApiError("INTERNAL_SERVER_ERROR", "Error interno del servidor", null)));
        }
    }

    // Endpoint para refrescar el token de autenticación
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponseDto>> refreshToken(@RequestBody String refreshToken) {
        try {
            Authentication authentication = userAuthProvider.validateRefreshToken(refreshToken);
            UserDto user = (UserDto) authentication.getPrincipal();
            String newToken = userAuthProvider.createToken(user.getEmail());
            AuthResponseDto response = AuthResponseDto.builder()
                    .token(newToken)
                    .user(user)
                    .build();
            return ResponseEntity.ok(ApiResponse.success(response, "Token refrescado con éxito."));
        } catch (JwtAuthenticationException e) {
            logger.warn("Error en refresco de token: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(new ApiError("UNAUTHORIZED", e.getMessage(), null)));
        } catch (Exception e) {
            logger.error("Error inesperado en refresco de token: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(new ApiError("INTERNAL_SERVER_ERROR", "Error interno del servidor", null)));
        }
    }

    // Endpoint para obtener información del usuario autenticado
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser(Authentication authentication) {
        try {
            String email = authentication.getName();
            UserDto user = userService.findByEmail(email);
            return ResponseEntity.ok(ApiResponse.success(user, "Usuario obtenido exitosamente."));
        } catch (EmailNotFoundException e) {  // Cambiado aquí
            logger.warn("Error al obtener usuario: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(new ApiError("NOT_FOUND", e.getMessage(), null)));
        } catch (Exception e) {
            logger.error("Error inesperado al obtener usuario: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(new ApiError("INTERNAL_SERVER_ERROR", "Error interno del servidor", null)));
        }
    }

    // Endpoint para activar la cuenta de un usuario
    @GetMapping("/activate")
    public ResponseEntity<ApiResponse<String>> activateAccount(@RequestParam("token") String token) {
        try {
            boolean activated = userService.activateUser(token);
            return ResponseEntity.ok(ApiResponse.success("Cuenta activada exitosamente!", "Cuenta activada exitosamente."));
        } catch (JwtAuthenticationException e) {
            logger.warn("Error al activar cuenta: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(new ApiError("BAD_REQUEST", e.getMessage(), null)));
        } catch (Exception e) {
            logger.error("Error inesperado al activar cuenta: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(new ApiError("INTERNAL_SERVER_ERROR", "Error interno del servidor", null)));
        }
    }

    // Endpoint para cerrar sesión del usuario
    @DeleteMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        // Verificar si el encabezado de autorización está presente y es válido
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(new ApiError("UNAUTHORIZED", "No se ha podido cerrar sesión. Token inválido.", null)));
        }

        String token = authorizationHeader.substring(7); // Extrae el token

        try {
            // Verificar si el token ya ha sido revocado
            if (userAuthProvider.isTokenRevoked(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error(new ApiError("UNAUTHORIZED", "El token ya ha sido revocado.", null)));
            }

            // Llama al método para revocar el token
            userAuthProvider.revokeToken(token);
            return ResponseEntity.ok(ApiResponse.success("Se ha cerrado la sesión correctamente", "Sesión cerrada con éxito."));
        } catch (Exception e) {
            logger.error("Error al cerrar sesión: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(new ApiError("INTERNAL_SERVER_ERROR", "Cerrar sesión fallida", null)));
        }
    }
}
