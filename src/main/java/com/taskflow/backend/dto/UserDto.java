package com.taskflow.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data

//Creacion de usuario DTO para la creacion del JSON del registro de usuario y sus endpoints.
public class UserDto {

    private Long id;
    private String name;
    private String first_surname;
    private String second_surname;
    private String id_card; //Cedula.
    private String phone_number;
    private String email;
    private String token;
}
