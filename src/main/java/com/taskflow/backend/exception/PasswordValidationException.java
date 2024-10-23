package com.taskflow.backend.exception;

public class PasswordValidationException extends RuntimeException {
    private final String code;
    
    public PasswordValidationException(String message, String code) {
        super(message);
        this.code = code;
    }
    
    public String getCode() {
        return code;
    }
}