package com.taskflow.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data

public class SignUpDto {

    private String name;
    private String first_surname;
    private String second_surname;
    private String id_card;
    private String phone_number;
    private String email;
    private char[] password;
}
