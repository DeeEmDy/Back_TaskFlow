package com.taskflow.backend.mappers;


import com.taskflow.backend.dto.SignUpDto;
import com.taskflow.backend.dto.UserDto;
import com.taskflow.backend.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "idImage", target = "idImage")
    @Mapping(source = "idRol", target = "idRol")
    @Mapping(target = "password", ignore = true) // La contraseña se maneja por separado
    User signUpToUser(SignUpDto signUpDto);

    UserDto toUserDto(User user);
}

