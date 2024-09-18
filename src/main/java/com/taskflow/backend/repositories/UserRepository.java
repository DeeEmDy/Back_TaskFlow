package com.taskflow.backend.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taskflow.backend.entities.User;

public interface UserRepository extends JpaRepository <User, Integer> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email); //Para verificar si el email ya existe

    boolean existsByIdCard(String idCard); //Para verificar si la cédula ya existe

    boolean existsByPhoneNumber(String phoneNumber); //Para verificar si el número de teléfono ya existe
}
