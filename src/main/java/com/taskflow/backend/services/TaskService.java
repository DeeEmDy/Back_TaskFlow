package com.taskflow.backend.services;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.taskflow.backend.dto.taskDtos.CreateTaskDto;
import com.taskflow.backend.entities.Task;
import com.taskflow.backend.entities.User;
import com.taskflow.backend.exception.TaskExceptions.TaskNotFoundException;
import com.taskflow.backend.exception.TaskExceptions.TaskTitleAlreadyExist;
import com.taskflow.backend.exception.UserIdNotFoundException;
import com.taskflow.backend.repositories.TaskRepository;
import com.taskflow.backend.repositories.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    // Método de servicio
    @Transactional
    public Task createTask(CreateTaskDto createTaskDto) {
        // Primero, verifica si la tarea ya existe por el título
        if (taskRepository.existsByTitle(createTaskDto.getTitle())) {
            throw new TaskTitleAlreadyExist("Ya existe una tarea con el título '" + createTaskDto.getTitle() + "'.");
        }

        User user = userRepository.findById(createTaskDto.getIdUser())
                .orElseThrow(() -> new UserIdNotFoundException("El ID de usuario " + createTaskDto.getIdUser() + " no existe."));

        Task task = new Task();

        // Asignación de valores desde el DTO a la entidad Task
        task.setIdUser(user);
        task.setTitle(createTaskDto.getTitle());
        task.setDescriptionTask(createTaskDto.getDescriptionTask());
        task.setExpirationTaskDate(createTaskDto.getExpirationTaskDate());
        task.setProgressTask(createTaskDto.getProgressTask());
        task.setFinalizationTaskDate(createTaskDto.getFinalizationTaskDate());

        task.setCreatedTaskDate(LocalDate.now());
        task.setStatus(true);
        task.setCreatedAt(Instant.now());
        task.setScore(calculateScore(task.getCreatedTaskDate(), task.getExpirationTaskDate()));

        return taskRepository.save(task);
    }

    // Calcular el score basado en el rango de fechas
    private Integer calculateScore(LocalDate createdTaskDate, LocalDate expirationTaskDate) {
        long daysBetween = ChronoUnit.DAYS.between(createdTaskDate, expirationTaskDate);
        if (daysBetween <= 5) {
            return 100; 
        }else if (daysBetween <= 10) {
            return 75; 
        }else {
            return 50;
        }
    }

    // Obtener todas las tareas
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    // Obtener una tarea por su ID
    public Task getTaskById(Integer id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("La tarea con el ID " + id + " no fue encontrada."));
    }

    // Actualizar una tarea existente
    @Transactional
    public Task updateTask(Integer id, CreateTaskDto createTaskDto) {
        Task existingTask = getTaskById(id);

        validateTaskTitle(createTaskDto, existingTask.getId());

        User user = userRepository.findById(createTaskDto.getIdUser())
                .orElseThrow(() -> new UserIdNotFoundException("El ID de usuario " + createTaskDto.getIdUser() + " no existe."));

        existingTask.setIdUser(user);
        existingTask.setTitle(createTaskDto.getTitle());
        existingTask.setDescriptionTask(createTaskDto.getDescriptionTask());
        existingTask.setExpirationTaskDate(createTaskDto.getExpirationTaskDate());
        existingTask.setProgressTask(createTaskDto.getProgressTask()); // Se usa el enum
        existingTask.setFinalizationTaskDate(createTaskDto.getFinalizationTaskDate());
        existingTask.setScore(calculateScore(existingTask.getCreatedTaskDate(), existingTask.getExpirationTaskDate()));
        existingTask.setUpdatedAt(Instant.now());

        return taskRepository.save(existingTask);
    }

    // Eliminar una tarea
    public void deleteTask(Integer id) {
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException("La tarea con el ID " + id + " no fue encontrada.");
        }
        taskRepository.deleteById(id);
    }

    private void validateTaskTitle(CreateTaskDto createTaskDto, Integer existingTaskId) {
        boolean titleExists = taskRepository.existsByTitle(createTaskDto.getTitle());
        if (titleExists && (existingTaskId == null || existingTaskId != taskRepository.findByTitle(createTaskDto.getTitle()).getId())) {
            throw new TaskTitleAlreadyExist("El título de la tarea '" + createTaskDto.getTitle() + "' ya existe.");
        }
    }
}
