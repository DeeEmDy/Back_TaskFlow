package com.taskflow.backend.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.taskflow.backend.dto.ApiError;
import com.taskflow.backend.dto.ApiResponse;
import com.taskflow.backend.dto.taskDtos.CreateTaskDto;
import com.taskflow.backend.dto.taskDtos.TaskDto;
import com.taskflow.backend.entities.Task;
import com.taskflow.backend.exception.UserIdNotFoundException;
import com.taskflow.backend.exception.TaskExceptions.TaskNotFoundException;
import com.taskflow.backend.exception.TaskExceptions.TaskTitleAlreadyExist;
import com.taskflow.backend.services.TaskService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping("/task")
public class TaskController {

    private final TaskService taskService;
    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Task>> createTask(@Valid @RequestBody CreateTaskDto createTaskDto) {
        logger.info("Iniciando a crear una nueva tarea con el título: {}", createTaskDto.getTitle());
        try {
            Task task = taskService.createTask(createTaskDto);
            logger.info("Tarea creada con el título: {}", createTaskDto.getTitle());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(task, "Tarea creada exitosamente"));
        } catch (UserIdNotFoundException ex) {
            logger.warn("El ID de usuario no fue encontrado: {}", createTaskDto.getIdUser());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(new ApiError("USUARIO NO ENCONTRADO", ex.getMessage(), null)));
        } catch (TaskTitleAlreadyExist ex) {
            logger.warn("Error al crear tarea: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT) // Usar 409 Conflict para títulos duplicados
                    .body(ApiResponse.error(new ApiError("TÍTULO YA EXISTE", ex.getMessage(), null)));
        } catch (RuntimeException ex) {
            logger.error("Error inesperado ocurrió durante la creación: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(new ApiError("Error interno en el servidor", "Un error ha ocurrido, por favor intente más tarde.", null)));
        }
    }

    @GetMapping("/getAll")
    public ResponseEntity<ApiResponse<List<Task>>> getAllTasks() {
        logger.info("Obteniendo todos los registros de tareas");
        List<Task> tasks = taskService.getAllTasks();
        if (tasks.isEmpty()) {
            logger.warn("No se han encontrado registros de tareas");
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(ApiResponse.error(new ApiError("NO SE HAN ENCONTRADO TAREAS", "No se han encontrado tareas.", null)));
        }
        logger.info("Obteniendo {} tareas", tasks.size());
        return ResponseEntity.ok(ApiResponse.success(tasks, "Se ha obtenido las tareas exitosamente"));
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<ApiResponse<Task>> getTaskById(@PathVariable Integer id) {
        logger.info("Obteniendo la tarea con el ID: {}", id);
        try {
            Task task = taskService.getTaskById(id);
            logger.info("Tarea encontrada con el ID: {}", id);
            return ResponseEntity.ok(ApiResponse.success(task, "Tarea encontrada con el ID " + id));
        } catch (TaskNotFoundException ex) {
            logger.warn("Tarea no encontrada con el ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(new ApiError("TAREA NO ENCONTRADA", "Tarea con el ID " + id + " no encontrada.", null)));
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<Task>> updateTask(@PathVariable Integer id, @Valid @RequestBody CreateTaskDto createTaskDto) {
        logger.info("Iniciando a actualizar la tarea con el ID: {}", id);
        try {
            Task updatedTask = taskService.updateTask(id, createTaskDto);
            logger.info("Tarea actualizada con el ID: {}", id);
            return ResponseEntity.ok(ApiResponse.success(updatedTask, "Tarea actualizada exitosamente"));
        } catch (TaskTitleAlreadyExist ex) {
            logger.error("Error al actualizar, ya existe una tarea con ese título: {}", createTaskDto.getTitle());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(new ApiError("TÍTULO YA EXISTENTE", "El título de la tarea ya existe.", null)));
        } catch (TaskNotFoundException ex) {
            logger.warn("Tarea no encontrada con el ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(new ApiError("TAREA NO ENCONTRADA", "Tarea con el ID " + id + " no encontrada.", null)));
        } catch (UserIdNotFoundException ex) {
            logger.warn("El ID de usuario no fue encontrado: {}", createTaskDto.getIdUser());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(new ApiError("USUARIO NO ENCONTRADO", "El ID de usuario " + createTaskDto.getIdUser() + " no existe.", null)));
        } catch (RuntimeException ex) {
            logger.error("Error inesperado ocurrió durante la actualización: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(new ApiError("Error interno en el servidor", "Un error ha ocurrido, por favor intente más tarde.", null)));
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable Integer id) {
        logger.info("Iniciando a eliminar la tarea con el ID: {}", id);
        try {
            taskService.deleteTask(id);
            logger.info("Tarea eliminada con el ID: {}", id);
            return ResponseEntity.ok(ApiResponse.success(null, "Tarea eliminada exitosamente"));
        } catch (TaskNotFoundException ex) {
            logger.warn("La tarea con el ID: {} no fue encontrada", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(new ApiError("TAREA NO ENCONTRADA", "Tarea con el ID " + id + " no fue encontrada.", null)));
        } catch (RuntimeException ex) {
            logger.error("Error inesperado ocurrió durante la eliminación: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(new ApiError("Error interno en el servidor", "Un error ha ocurrido, por favor intente más tarde.", null)));
        }
    }
}
