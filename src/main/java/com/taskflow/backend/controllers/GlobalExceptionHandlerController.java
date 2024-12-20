package com.taskflow.backend.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.taskflow.backend.dto.ApiError;
import com.taskflow.backend.dto.ApiResponse;
import com.taskflow.backend.dto.ValidationError;
import com.taskflow.backend.exception.IdCardAlreadyExistsException;
import com.taskflow.backend.exception.InvalidCredentialsException;
import com.taskflow.backend.exception.PasswordMismatchException;
import com.taskflow.backend.exception.PasswordValidationException;
import com.taskflow.backend.exception.PhoneNumberAlreadyExistsException;
import com.taskflow.backend.exception.TaskExceptions.TaskNotFoundException;
import com.taskflow.backend.exception.TaskExceptions.TaskTitleAlreadyExist;
import com.taskflow.backend.exception.UserAlreadyExistsException;
import com.taskflow.backend.exception.UserIdNotFoundException;

@ControllerAdvice
public class GlobalExceptionHandlerController {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        ApiError apiError = new ApiError("USER_ALREADY_EXISTS", ex.getMessage(), null);
        return new ResponseEntity<>(ApiResponse.error(apiError), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PhoneNumberAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>> handlePhoneNumberAlreadyExistsException(PhoneNumberAlreadyExistsException ex) {
        ApiError apiError = new ApiError("PHONE_NUMBER_ALREADY_EXISTS", ex.getMessage(), null);
        return new ResponseEntity<>(ApiResponse.error(apiError), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IdCardAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>> handleIdCardAlreadyExistsException(IdCardAlreadyExistsException ex) {
        ApiError apiError = new ApiError("ID_CARD_ALREADY_EXISTS", ex.getMessage(), null);
        return new ResponseEntity<>(ApiResponse.error(apiError), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PasswordValidationException.class)
    public ResponseEntity<ApiResponse<Object>> handlePasswordValidationException(PasswordValidationException ex) {
        ApiError apiError = new ApiError(ex.getCode(), ex.getMessage(), null);
        return new ResponseEntity<>(ApiResponse.error(apiError), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PasswordMismatchException.class) // Nuevo manejador para la excepción de coincidencia de contraseñas
    public ResponseEntity<ApiResponse<Object>> handlePasswordMismatchException(PasswordMismatchException ex) {
        ApiError apiError = new ApiError("PASSWORD_MISMATCH", ex.getMessage(), null);
        return new ResponseEntity<>(ApiResponse.error(apiError), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationExceptions(MethodArgumentNotValidException e) {
        List<ValidationError> validationErrors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> new ValidationError(error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());

        ApiError apiError = new ApiError("VALIDATION_ERROR", "Errores de validación en los datos ingresados", validationErrors);
        return new ResponseEntity<>(ApiResponse.error(apiError), HttpStatus.BAD_REQUEST);
    }

    //Excepciones para el manejo de errores en la creación de tareas.
    @ExceptionHandler(UserIdNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleUserIdNotFoundException(UserIdNotFoundException ex) {
        ApiError apiError = new ApiError("USER_ID_NOT_FOUND", ex.getMessage(), null);
        return new ResponseEntity<>(ApiResponse.error(apiError), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleTaskNotFoundException(TaskNotFoundException ex) {
        ApiError apiError = new ApiError("TASK_NOT_FOUND", ex.getMessage(), null);
        return new ResponseEntity<>(ApiResponse.error(apiError), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(TaskTitleAlreadyExist.class)
    public ResponseEntity<ApiResponse<Object>> handleTaskTitleAlreadyExist(TaskTitleAlreadyExist ex) {
        ApiError apiError = new ApiError("TASK_TITLE_ALREADY_EXISTS", ex.getMessage(), null);
        return new ResponseEntity<>(ApiResponse.error(apiError), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidCredentialsException(InvalidCredentialsException ex) {
        ApiError apiError = new ApiError("INVALID_CREDENTIALS", ex.getMessage(), null);
        return new ResponseEntity<>(ApiResponse.error(apiError), HttpStatus.BAD_REQUEST);
    }


}
