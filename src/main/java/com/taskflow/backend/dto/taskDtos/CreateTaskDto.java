package com.taskflow.backend.dto.taskDtos;

import java.time.LocalDate;

import com.taskflow.backend.enums.ProgressTaskCategoriesEnum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateTaskDto {

    // PK del usuario para asociarle la tarea.
    @NotNull(message = "El ID de usuario no puede ser nulo")
    private Integer idUser;

    @NotBlank(message = "El título no puede estar vacío")
    @Size(min = 5, max = 20, message = "El título debe tener entre 5 y 20 caracteres")
    @Pattern(regexp = "^[A-Za-záéíóúÁÉÍÓÚñÑ´ ]+$", message = "El título solo puede contener letras y espacios")
    private String title;

    @NotBlank(message = "La descripción no puede estar vacía")
    @Size(min = 1, max = 200, message = "La descripción debe tener entre 1 y 200 caracteres")
    @Pattern(regexp = "^[A-Za-záéíóúÁÉÍÓÚñÑ´ ]+$", message = "La descripción solo puede contener letras y espacios")
    private String descriptionTask;

    @NotNull(message = "La fecha de expiración no puede ser nula")
    private LocalDate expirationTaskDate;

    @NotNull(message = "La categoría de la tarea no puede ser nula")
    private ProgressTaskCategoriesEnum progressTask;

    @NotNull(message = "La fecha de finalización no puede ser nula")
    private LocalDate finalizationTaskDate;
}

