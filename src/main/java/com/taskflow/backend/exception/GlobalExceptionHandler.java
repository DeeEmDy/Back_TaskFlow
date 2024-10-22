// src/main/java/com/taskflow/backend/exception/GlobalExceptionHandler.java
package com.taskflow.backend.exception;

import com.taskflow.backend.dto.ApiError;
import com.taskflow.backend.dto.ApiResponse;
import com.taskflow.backend.dto.ValidationError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<ValidationError> validationErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> new ValidationError(error.getField(), error.getDefaultMessage()))
            .collect(Collectors.toList());

        ApiError apiError = new ApiError(
            "VALIDATION_ERROR",
            "Error de validación en los datos ingresados",
            validationErrors
        );

        return new ResponseEntity<>(ApiResponse.error(apiError), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        ApiError apiError = new ApiError(
            "RESOURCE_NOT_FOUND",
            ex.getMessage(),
            null
        );
        return new ResponseEntity<>(ApiResponse.error(apiError), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Object>> handleDuplicateResource(DuplicateResourceException ex) {
        ApiError apiError = new ApiError(
            "DUPLICATE_RESOURCE",
            ex.getMessage(),
            null
        );
        return new ResponseEntity<>(ApiResponse.error(apiError), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneralException(Exception ex) {
        ApiError apiError = new ApiError(
            "INTERNAL_SERVER_ERROR",
            "Ha ocurrido un error interno en el servidor",
            null
        );
        return new ResponseEntity<>(ApiResponse.error(apiError), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}