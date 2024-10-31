package com.taskflow.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SignUpDto {

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(min = 3, max = 12, message = "El nombre debe tener entre 3 y 12 caracteres")
    @Pattern(regexp = "^[A-Za-záéíóúÁÉÍÓÚñÑ´ ]+$", message = "El nombre solo puede contener letras y espacios")
    private String name;

    @NotBlank(message = "El primer apellido no puede estar vacío")
    @Size(min = 4, max = 15, message = "El primer apellido debe tener entre 4 y 15 caracteres")
    @Pattern(regexp = "^[A-Za-záéíóúÁÉÍÓÚñÑ´ ]+$", message = "El primer apellido solo puede contener letras y espacios")
    private String firstSurname;

    @NotBlank(message = "El segundo apellido no puede estar vacío")
    @Size(min = 4, max = 15, message = "El segundo apellido debe tener entre 4 y 15 caracteres")
    @Pattern(regexp = "^[A-Za-záéíóúÁÉÍÓÚñÑ´ ]+$", message = "El segundo apellido solo puede contener letras y espacios")
    private String secondSurname;

    @NotBlank(message = "El número de cédula no puede estar vacío")
    @Pattern(regexp = "^\\d{9,12}$", message = "El número de cédula debe tener entre 9 y 12 dígitos")
    private String idCard;

    @NotBlank(message = "El número de teléfono no puede estar vacío")
    @Pattern(regexp = "^\\+?\\d{8,15}$", message = "El número de teléfono debe ser válido y contener entre 8 y 15 dígitos")
    private String phoneNumber;

    @NotNull(message = "El ID de la imagen no puede ser nulo")
    private Integer idImage;

    @NotNull(message = "El ID del rol no puede ser nulo")
    private Integer idRol;

    @NotBlank(message = "El correo electrónico no puede estar vacío")
    @Email(message = "El formato del correo electrónico es inválido")
    private String email;

    @NotBlank(message = "La contraseña no puede estar vacía")
    @Pattern(
        regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,}$",
        message = "La contraseña debe tener al menos 8 caracteres, incluir una letra mayúscula, una letra minúscula y un número"
    )
    private String password;
}
