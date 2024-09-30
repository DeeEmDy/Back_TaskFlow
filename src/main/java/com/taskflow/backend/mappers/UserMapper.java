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
import com.taskflow.backend.enums.RoleTypeEnum;

@Mapper(componentModel = "spring", uses = {ImageMapper.class, RoleMapper.class})
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    // Convertir SignUpDto a User
    @Mappings({
        @Mapping(source = "idImage", target = "idImage", qualifiedByName = "mapIdToImage"),
        @Mapping(source = "idRol", target = "role", qualifiedByName = "mapIdToRole"),
    })
    User signUpToUser(SignUpDto signUpDto);

    // Convertir User a UserDto
    @Mappings({
        @Mapping(source = "role", target = "role", qualifiedByName = "mapRoleToRole"), // Mapeo directo
        @Mapping(source = "idImage", target = "idImage"),  // Usamos ImageMapper para convertir Image a ImageDto
    })
    UserDto toUserDto(User user);

    // Convertir UserDto a User
    @Mappings({
        @Mapping(source = "role", target = "role", qualifiedByName = "mapRoleDtoToRole"),
        @Mapping(source = "idImage", target = "idImage")  // Usamos ImageMapper para convertir ImageDto a Image
    })
    User toUser(UserDto userDto);

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
        return Collections.singletonList(role.getRolName().name()); // Usamos el nombre del enum
    }

    @Named("mapRoleToRole")
    default RoleDto mapRoleToRole(Rol role) {
        if (role == null) {
            return null;
        }
        return new RoleDto(role.getId(), role.getRolName().name(), role.getStatus());
    }

    @Named("mapRoleDtoToRole")
    default Rol mapRoleDtoToRole(RoleDto roleDto) {
        if (roleDto == null) {
            return null;
        }
        Rol role = new Rol();
        role.setId(roleDto.getId());
        role.setRolName(RoleTypeEnum.valueOf(roleDto.getRolName())); // Convertimos el nombre a RoleTypeEnum
        return role;
    }
}
