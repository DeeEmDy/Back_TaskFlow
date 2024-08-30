package com.taskflow.backend.mappers;


import com.taskflow.backend.dto.SignUpDto;
import com.taskflow.backend.dto.UserDto;
import com.taskflow.backend.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toUserDto(User user);

    @Mapping(target = "password", ignore = true) //Acá se ignora el campo de la contraseña, ya que va hasheada y no será la misma.
    User signUpToUser(SignUpDto userDto);
}
