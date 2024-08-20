package com.taskflow.backend.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
public class MessagesController {

    @GetMapping("/messages")
    public ResponseEntity<List<String>> getMessages() {
        return  ResponseEntity.ok(Arrays.asList("First Message", "Second Message", "Third Message", "Hola soy el ultimo mensaje"));
    }
}
