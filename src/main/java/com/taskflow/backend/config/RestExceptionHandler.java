package com.taskflow.backend.config;

import com.taskflow.backend.dto.ErrorDto;
import com.taskflow.backend.exception.AppException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(value = {AppException.class})
    @ResponseBody
    public ResponseEntity<ErrorDto> handleAppException(AppException ex) {

        return ResponseEntity.status(ex.getCode())
                .body(new ErrorDto(ex.getMessage()));
    }
}
