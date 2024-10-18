package com.taskflow.backend.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.taskflow.backend.dto.ErrorDto;
import com.taskflow.backend.dto.SignUpDto;
import com.taskflow.backend.dto.UserDto;
import com.taskflow.backend.exception.IdCardAlreadyExistsException;
import com.taskflow.backend.exception.PhoneNumberAlreadyExistsException;
import com.taskflow.backend.exception.UserAlreadyExistsException;
import com.taskflow.backend.services.UserService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    // Endpoint para crear un usuario con el método createUser. 
    @PostMapping("/create")
    public ResponseEntity<?> createUser(@RequestBody SignUpDto newUser) {
        logger.info("Attempting to create new user with email: {}", newUser.getEmail());
        try {
            UserDto user = userService.createUser(newUser);
            logger.info("User created successfully with email: {}", newUser.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (UserAlreadyExistsException | IdCardAlreadyExistsException | PhoneNumberAlreadyExistsException ex) {
            logger.error("Error creating user: {}", ex.getMessage());
            ErrorDto errorResponse = new ErrorDto(ex.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (IllegalArgumentException ex) {
            logger.error("Error: {}", ex.getMessage());
            ErrorDto errorResponse = new ErrorDto("Error de argumento: " + ex.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (RuntimeException ex) {
            logger.error("Unexpected error: {}", ex.getMessage());
            ErrorDto errorResponse = new ErrorDto("Error inesperado: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        logger.info("Request to get all users received");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logger.info("User {} with roles {} is attempting to access /user/getAll",
                auth.getName(),
                auth.getAuthorities());
        List<UserDto> users = userService.findAll();
        logger.info("Returning {} users", users.size());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Integer id) {
        logger.info("Attempting to get user with ID: {}", id);
        UserDto user = userService.findById(id);
        if (user != null) {
            logger.info("User found with ID: {}", id);
            return ResponseEntity.ok(user);
        } else {
            logger.warn("User not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Integer id, @RequestBody SignUpDto updatedUser) {
        logger.info("Attempting to update user with ID: {}", id);
        UserDto user = userService.update(id, updatedUser);
        logger.info("User updated successfully with ID: {}", id);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        logger.info("Attempting to delete user with ID: {}", id);
        userService.delete(id);
        logger.info("User deleted successfully with ID: {}", id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/update-password")
    public ResponseEntity<Void> updatePassword(@RequestParam String email, @RequestParam String newPassword) {
        logger.info("Attempting to update password for user with email: {}", email);
        userService.updatePassword(email, newPassword);
        logger.info("Password updated successfully for user with email: {}", email);
        return ResponseEntity.noContent().build();
    }
}
