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
import org.springframework.web.bind.annotation.*;

import com.taskflow.backend.dto.ApiResponse;
import com.taskflow.backend.dto.SignUpDto;
import com.taskflow.backend.dto.UserDto;
import com.taskflow.backend.exception.IdCardAlreadyExistsException;
import com.taskflow.backend.exception.PhoneNumberAlreadyExistsException;
import com.taskflow.backend.exception.UserAlreadyExistsException;
import com.taskflow.backend.services.UserService;
import com.taskflow.backend.dto.ApiError;

import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
@Validated
public class UserController {

    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<UserDto>> createUser(@Valid @RequestBody SignUpDto newUser) {
        logger.info("Attempting to create new user with email: {}", newUser.getEmail());
        try {
            UserDto user = userService.createUser(newUser);
            logger.info("User created successfully with email: {}", newUser.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(user, "User created successfully"));
        } catch (UserAlreadyExistsException ex) {
            logger.error("Error creating user: User already exists with email: {}", newUser.getEmail());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(new ApiError("USER_ALREADY_EXISTS", "User with email " + newUser.getEmail() + " already exists.", null)));
        } catch (IdCardAlreadyExistsException ex) {
            logger.error("Error creating user: ID card already exists: {}", newUser.getIdCard());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(new ApiError("ID_CARD_ALREADY_EXISTS", "ID card " + newUser.getIdCard() + " already exists.", null)));
        } catch (PhoneNumberAlreadyExistsException ex) {
            logger.error("Error creating user: Phone number already exists: {}", newUser.getPhoneNumber());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(new ApiError("PHONE_NUMBER_ALREADY_EXISTS", "Phone number " + newUser.getPhoneNumber() + " already exists.", null)));
        } catch (IllegalArgumentException ex) {
            logger.error("Invalid arguments: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(new ApiError("INVALID_ARGUMENTS", "Invalid arguments: " + ex.getMessage(), null)));
        } catch (RuntimeException ex) {
            logger.error("Unexpected error occurred: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(new ApiError("INTERNAL_SERVER_ERROR", "An unexpected error occurred. Please try again later.", null)));
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
    public ResponseEntity<ApiResponse<UserDto>> updateUser(@PathVariable Integer id, @Valid @RequestBody SignUpDto updatedUser) {
        logger.info("Iniciando a actualizar el registro de usuario con el ID: {}", id);
        try {
            UserDto user = userService.update(id, updatedUser);
            logger.info("Usuario actualizado con el ID: {}", id);
            return ResponseEntity.ok(ApiResponse.success(user, "Usuario actualizado con éxito"));
        } catch (RuntimeException ex) {
            logger.warn("Ha ocurrido un error al actualizar el usuario con el ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(new ApiError("USUARIO NO ENCONTRADO", "Ha ocurrido un error al actualizar al usuario: " + ex.getMessage(), null)));
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
    public ResponseEntity<ApiResponse<Void>> updatePassword(@RequestParam String email, @RequestParam String newPassword) {
        logger.info("Iniciando a cambiar la contraseña del usuario con el email: {}", email);
        try {
            userService.updatePassword(email, newPassword);
            logger.info("Contraseña cambiada para el usuario con el email: {}", email);
            return ResponseEntity.ok()
                    .body(ApiResponse.success(null, "Contraseña actualizada exitosamente"));
        } catch (UsernameNotFoundException ex) {
            logger.warn("No se ha encontrado un usuario con el email: {}", email);
            ApiError apiError = new ApiError("USUARIO NO ENCONTRADO", "Error al cambiar la contraseña del usuario con el email " + email + " not found.", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(apiError));
        }
    }

}
