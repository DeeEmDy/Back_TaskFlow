package com.taskflow.backend.exception;

import org.springframework.http.HttpStatus;

public class AppException extends RuntimeException { //Clase para manejar las respuestas HTTP como excepción personalizadas.

    private final HttpStatus code;

    public AppException(String message, HttpStatus code) {

        super(message);
        this.code = code;

    }

    public HttpStatus getCode() {

        return code;
    }
}
