package com.taskflow.backend.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.taskflow.backend.entities.Rol;
import com.taskflow.backend.enums.RoleTypeEnum;

@Repository
public interface RolRepository extends JpaRepository<Rol, Integer> {
    Optional<Rol> findByRolName(RoleTypeEnum rolName); // Puedes mantener este método si es necesario
}
