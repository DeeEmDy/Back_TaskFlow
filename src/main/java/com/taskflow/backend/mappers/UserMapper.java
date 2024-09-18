package com.taskflow.backend.mappers;

import java.util.Collections;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import com.taskflow.backend.dto.SignUpDto;
import com.taskflow.backend.dto.UserDto;
import com.taskflow.backend.dto.ImageDto;
import com.taskflow.backend.dto.RoleDto;
import com.taskflow.backend.entities.Image;
import com.taskflow.backend.entities.Rol;
import com.taskflow.backend.entities.User;

@Mapper(componentModel = "spring", uses = {ImageMapper.class, RoleMapper.class})
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    // Convertir SignUpDto a User
    @Mappings({
        @Mapping(source = "idImage", target = "idImage", qualifiedByName = "mapIdToImage"),
        @Mapping(source = "idRol", target = "role", qualifiedByName = "mapIdToRole"),
        @Mapping(target = "createdAt", ignore = true),  // No hay propiedad equivalente en SignUpDto
        @Mapping(target = "updatedAt", ignore = true)   // No hay propiedad equivalente en SignUpDto
    })
    User signUpToUser(SignUpDto signUpDto);

    // Convertir User a UserDto
    @Mappings({
        @Mapping(source = "role", target = "role"),  // Usamos RoleMapper para convertir Rol a RoleDto
        @Mapping(source = "idImage", target = "idImage"),  // Usamos ImageMapper para convertir Image a ImageDto
        @Mapping(source = "createdAt", target = "createdAt"),
        @Mapping(source = "updatedAt", target = "updatedAt"),
        @Mapping(target = "roles", source = "role", qualifiedByName = "mapRoleToRoles")
    })
    UserDto toUserDto(User user);

    // Métodos de conversión personalizados
    @Named("mapIdToImage")
    default Image mapIdToImage(Integer id) {
        if (id == null) {
            return null;
        }
        Image image = new Image();
        image.setId(id);
        return image;
    }

    @Named("mapIdToRole")
    default Rol mapIdToRole(Integer id) {
        if (id == null) {
            return null;
        }
        Rol role = new Rol();
        role.setId(id);
        return role;
    }

    @Named("mapRoleToRoles")
    default List<String> mapRoleToRoles(Rol role) {
        if (role == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(role.getRolName());
    }
}
