package com.taskflow.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST) //Para brindar error 400 en api.
public class IdCardAlreadyExistsException extends RuntimeException {

    public IdCardAlreadyExistsException(String message) {
        super(message);
    }
}
