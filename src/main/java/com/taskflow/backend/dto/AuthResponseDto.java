package com.taskflow.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class AuthResponseDto {

    private String token; // Token de autenticación
    private UserDto user; // Datos del usuario
}
