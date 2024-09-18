package com.taskflow.backend.dto;

import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class UserDto {

    private Integer id;
    private String name;
    private String firstSurname;
    private String secondSurname;
    private String idCard;
    private String phoneNumber;
    private ImageDto idImage;
    private RoleDto role;
    private String email;
    private List<String> roles; // Lista de roles si tienes múltiples roles
    private Boolean userVerified;
    private Boolean status;
    private Instant createdAt;
    private Instant updatedAt;
}