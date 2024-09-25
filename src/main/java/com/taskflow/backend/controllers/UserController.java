package com.taskflow.backend.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.taskflow.backend.dto.SignUpDto;
import com.taskflow.backend.dto.UserDto;
import com.taskflow.backend.services.UserService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    //Crear un registro de usuario
    @PostMapping("/create")
    public ResponseEntity<UserDto> createUser(@RequestBody SignUpDto signUpDto) {
        UserDto createdUser = userService.register(signUpDto);
        return ResponseEntity.ok(createdUser);
    }

    //Leer todos los usuarios
    @GetMapping("/getAll")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userService.findAll();
        return ResponseEntity.ok(users);
    }

    //Leer un usuario por ID
    @GetMapping("/getById/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Integer id) {
        UserDto user = userService.findById(id);
        if (user == null) {
            return ResponseEntity.notFound().build(); // Devuelve 404 si no se encuentra el usuario
        }
        return ResponseEntity.ok(user);
    }


    //Actualizar un usuario
    @PutMapping("/update/{id}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable Integer id, 
            @RequestBody SignUpDto updatedUser) {
        UserDto user = userService.update(id, updatedUser);
        return ResponseEntity.ok(user);
    }

    //Eliminar un usuario
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    //Actualizar la contraseña de un usuario
    @PatchMapping("/update-password")
    public ResponseEntity<Void> updatePassword(
            @RequestParam String email, 
            @RequestParam String newPassword) {
        userService.updatePassword(email, newPassword);
        return ResponseEntity.noContent().build();
    }
}
