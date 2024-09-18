package com.taskflow.backend.mappers;

import org.mapstruct.Mapper;

import com.taskflow.backend.dto.RoleDto;
import com.taskflow.backend.entities.Rol;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    RoleDto toRoleDto(Rol role);
    Rol toRole(RoleDto roleDto);
}
