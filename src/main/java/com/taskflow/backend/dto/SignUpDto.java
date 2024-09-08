package com.taskflow.backend.dto;

import com.taskflow.backend.entities.Image;
import com.taskflow.backend.entities.Rol;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SignUpDto {

    private String name;
    private String firstSurname;
    private String secondSurname;
    private String idCard;
    private String phoneNumber;
    private Image idImage;
    private Rol idRol;
    private String email;
    private String password;
    private boolean userVerified;
    private boolean status;
}
