package com.taskflow.backend.repositories;

import java.util.Optional;
//import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.taskflow.backend.entities.ActivationToken;

@Repository
public interface ActivationTokenRepository extends JpaRepository<ActivationToken, String> {
    Optional<ActivationToken> findByToken(String token);
}
