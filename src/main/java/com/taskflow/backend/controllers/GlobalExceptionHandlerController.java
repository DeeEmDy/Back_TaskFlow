package com.taskflow.backend.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.taskflow.backend.exception.IdCardAlreadyExistsException;
import com.taskflow.backend.exception.PhoneNumberAlreadyExistsException;
import com.taskflow.backend.exception.UserAlreadyExistsException;

@ControllerAdvice
public class GlobalExceptionHandlerController {

    // Manejador de excepciones para UserAlreadyExistsException
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<String> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    // Manejador de excepciones para PhoneNumberAlreadyExistsException
    @ExceptionHandler(PhoneNumberAlreadyExistsException.class)
    public ResponseEntity<String> handlePhoneNumberAlreadyExistsException(PhoneNumberAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    // Manejador de excepciones para IdCardAlreadyExistsException
    @ExceptionHandler(IdCardAlreadyExistsException.class)
    public ResponseEntity<String> handleIdCardAlreadyExistsException(IdCardAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

}
