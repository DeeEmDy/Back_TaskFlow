package com.taskflow.backend.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import com.taskflow.backend.dto.SignUpDto;
import com.taskflow.backend.dto.UserDto;
import com.taskflow.backend.services.UserService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    // Crear un registro de usuario
    @PostMapping("/create")
    public ResponseEntity<UserDto> createUser(@RequestBody SignUpDto signUpDto) {
        UserDto createdUser = userService.register(signUpDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    // Leer todos los usuarios
    @GetMapping("/getAll")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        logger.info("Request to get all users received");
        List<UserDto> users = userService.findAll();
        return ResponseEntity.ok(users);
    }

    // Leer un usuario por ID
    @GetMapping("/getById/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Integer id) {
        UserDto user = userService.findById(id);
        return user != null ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
    }

    // Actualizar un usuario
    @PutMapping("/update/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Integer id, @RequestBody SignUpDto updatedUser) {
        UserDto user = userService.update(id, updatedUser);
        return ResponseEntity.ok(user);
    }

    // Eliminar un usuario
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Actualizar la contraseña de un usuario
    @PatchMapping("/update-password")
    public ResponseEntity<Void> updatePassword(@RequestParam String email, @RequestParam String newPassword) {
        userService.updatePassword(email, newPassword);
        return ResponseEntity.noContent().build();
    }
}
