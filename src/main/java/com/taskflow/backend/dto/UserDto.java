package com.taskflow.backend.dto;

import com.taskflow.backend.entities.Image;
import com.taskflow.backend.entities.Rol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class UserDto {

    private int id;
    private String name;
    private String firstSurname;
    private String secondSurname;
    private String idCard; // Cédula
    private String phoneNumber;
    private String email;
    private Image idImage;
    private Rol idRol;
    private Boolean status;
    private String token;
}
