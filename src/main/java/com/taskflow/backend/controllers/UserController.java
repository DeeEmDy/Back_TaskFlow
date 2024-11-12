package com.taskflow.backend.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taskflow.backend.dto.ApiError;
import com.taskflow.backend.dto.ApiResponse;
import com.taskflow.backend.dto.CreateUserDto;
import com.taskflow.backend.dto.UpdatePasswordDto;
import com.taskflow.backend.dto.UserDto;
import com.taskflow.backend.exception.IdCardAlreadyExistsException;
import com.taskflow.backend.exception.ImageNotFoundException;
import com.taskflow.backend.exception.PasswordValidationException;
import com.taskflow.backend.exception.PhoneNumberAlreadyExistsException;
import com.taskflow.backend.exception.RoleNotFoundException;
import com.taskflow.backend.exception.UserAlreadyExistsException;
import com.taskflow.backend.services.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
@Validated
public class UserController {

    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<UserDto>> createUser(@Valid @RequestBody CreateUserDto newUser) {
        logger.info("Iniciando con la creación del usuario: {}", newUser.getEmail());
        try {
            UserDto user = userService.createUser(newUser);
            logger.info("Usuario creado con el email: {}", newUser.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(user, "Usuario creado exitosamente"));
        } catch (IdCardAlreadyExistsException | PhoneNumberAlreadyExistsException | UserAlreadyExistsException e) {
            logger.warn("Error en la creación: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(new ApiError("CONFLICT", e.getMessage(), null)));
        } catch (ImageNotFoundException e) {
            logger.warn("Error en la creación: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(new ApiError("BAD_REQUEST", e.getMessage(), null)));
        } catch (RoleNotFoundException e) {
            logger.warn("Error en la creación: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(new ApiError("NOT_FOUND", e.getMessage(), null)));
        } catch (IllegalArgumentException e) {
            logger.error("Argumentos inválidos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(new ApiError("BAD_REQUEST", e.getMessage(), null)));
        } catch (RuntimeException e) {
            logger.error("Error inesperado: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(new ApiError("INTERNAL_SERVER_ERROR", "Error interno del servidor", null)));
        }
    }

    @GetMapping("/getAll")
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsers() {
        logger.info("Request to get all users received");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logger.info("User {} with roles {} is attempting to access /user/getAll", auth.getName(), auth.getAuthorities());

        List<UserDto> users = userService.findAll();
        if (users.isEmpty()) {
            logger.warn("No users found");
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(ApiResponse.error(new ApiError("NO_USERS_FOUND", "No users found.", null)));
        }

        logger.info("Returning {} users", users.size());
        return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved successfully"));
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable Integer id) {
        logger.info("Attempting to get user with ID: {}", id);
        try {
            UserDto user = userService.findById(id);
            logger.info("Usuario encontrado con el ID: {}", id);
            return ResponseEntity.ok(ApiResponse.success(user, "Usuario encontrado con el ID " + id));
        } catch (RuntimeException ex) {
            logger.warn("No se ha encontrado un usuario con el ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(new ApiError("USUARIO NO ENCONTRADO", "El usuario con el ID " + id + " no ha sido encontrado.", null)));
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<UserDto>> updateUser(@PathVariable Integer id, @Valid @RequestBody CreateUserDto updatedUser) {
        logger.info("Iniciando a actualizar el registro de usuario con el ID: {}", id);
        try {
            UserDto user = userService.update(id, updatedUser);
            logger.info("Usuario actualizado con el ID: {}", id);
            return ResponseEntity.ok(ApiResponse.success(user, "Usuario actualizado con éxito"));
        } catch (UserAlreadyExistsException e) {
            logger.warn("Error al actualizar: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(new ApiError("CONFLICT", e.getMessage(), null)));
        } catch (ImageNotFoundException e) {
            logger.warn("Error en la actualización: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(new ApiError("BAD_REQUEST", e.getMessage(), null)));
        } catch (RoleNotFoundException e) {
            logger.warn("Error en la actualización: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(new ApiError("NOT_FOUND", e.getMessage(), null)));
        } catch (IllegalArgumentException e) {
            logger.error("Argumentos inválidos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(new ApiError("BAD_REQUEST", e.getMessage(), null)));
        } catch (RuntimeException e) {
            logger.error("Error inesperado: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(new ApiError("INTERNAL_SERVER_ERROR", "Error interno del servidor", null)));
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Integer id) {
        logger.info("Iniciando la eliminación del registro de usuario con el ID: {}", id);
        try {
            userService.delete(id);
            logger.info("Usuario eliminado existosamente con el ID: {}", id);
            // Aquí se utiliza .build() porque no se espera un cuerpo en la respuesta
            return ResponseEntity.noContent().build();
        } catch (RuntimeException ex) {
            logger.warn("Error al eliminar al usuario con el ID: {}", id);
            ApiError apiError = new ApiError("USUARIO NO ENCONTRADO", "Error al eliminar al usuario con el ID " + id + " no ha sido encontrado.", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(apiError));
        }
    }

    @PatchMapping("/update-password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(@Valid @RequestBody UpdatePasswordDto updatePasswordDto) {
        logger.info("Iniciando cambio de contraseña para el usuario: {}", updatePasswordDto.getEmail());

        try {
            userService.updatePassword(updatePasswordDto);
            return ResponseEntity.ok()
                    .body(ApiResponse.success(null, "Contraseña actualizada exitosamente"));
        } catch (UsernameNotFoundException ex) {
            logger.warn("Usuario no encontrado: {}", ex.getMessage());
            ApiError apiError = new ApiError("USUARIO_NO_ENCONTRADO", "No se encontró un usuario con el email: " + updatePasswordDto.getEmail(), null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(apiError));
        } catch (PasswordValidationException ex) {
            logger.warn("Error en validación de contraseña: {}", ex.getMessage());
            ApiError apiError;

            // Manejo específico para la validación de contraseñas que no coinciden
            if (ex.getCode().equals("PASSWORD_MISMATCH")) {
                apiError = new ApiError("VALIDATION_PASSWORDS_ERROR", "Error al cambiar la contraseña, las contraseñas ingresadas no coinciden.", null);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error(apiError));
            }

            // Manejo genérico para otros errores de validación
            apiError = new ApiError(ex.getCode(), ex.getMessage(), null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(apiError));
        } catch (Exception ex) {
            logger.error("Error inesperado: {}", ex.getMessage(), ex);
            ApiError apiError = new ApiError("ERROR_INTERNO", "Ha ocurrido un error inesperado en el servidor", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(apiError));
        }
    }

}
