package com.taskflow.backend.services;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class TokenService {

    public String generateActivationToken() {
        return UUID.randomUUID().toString();
    }

    public Instant getTokenExpiration() {
        // Definir el tiempo de expiración del token, por ejemplo, 24 horas.
        return Instant.now().plusSeconds(86400);
    }
}
