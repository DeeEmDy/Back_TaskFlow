package com.taskflow.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
@SpringBootApplication
@OpenAPIDefinition(
        info = @io.swagger.v3.oas.annotations.info.Info(
                title = "TaskFlow API",
                version = "1.0", // API version 10/29/2024.
                description = "TaskFlow API Documentation"
        )
)

public class TaskflowBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskflowBackendApplication.class, args);
    }

}
