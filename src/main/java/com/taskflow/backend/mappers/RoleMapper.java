package com.taskflow.backend.mappers;

import org.springframework.stereotype.Component;

import com.taskflow.backend.dto.RoleDto;
import com.taskflow.backend.entities.Rol;
import com.taskflow.backend.enums.RoleTypeEnum;

@Component
public class RoleMapper {

    public RoleTypeEnum toEnum(Rol role) {
        if (role == null) {
            return null;
        }
        return role.getRolName(); // Devuelve directamente el enum
    }

    public Rol toEntity(RoleTypeEnum roleTypeEnum) {
        if (roleTypeEnum == null) {
            return null;
        }
        Rol role = new Rol();
        // Asigna el enum directamente
        role.setRolName(roleTypeEnum);
        return role;
    }

    // Convertir RoleDto a Rol
    public Rol toEntity(RoleDto roleDto) {
        if (roleDto == null) {
            return null;
        }
        Rol rol = new Rol();
        rol.setId(roleDto.getId());
        rol.setRolName(RoleTypeEnum.valueOf(roleDto.getRolName())); // Convierte de String a enum
        rol.setStatus(roleDto.isStatus());
        return rol;
    }

    public RoleDto toRoleDto(Rol rol) {
        if (rol == null) {
            return null;
        }
        return new RoleDto(rol.getId(), rol.getRolName().name(), rol.getStatus());
    }
    
}
