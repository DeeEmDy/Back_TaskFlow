package com.taskflow.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SignUpDto {

    private Integer id;
    private String name;
    private String firstSurname;
    private String secondSurname;
    private String idCard;
    private String phoneNumber;
    private Integer idImage; // ID de la imagen
    private Integer idRol; // ID del rol
    private String email;
    private String password;
    private Boolean  userVerified;
    private Boolean  status;
}
