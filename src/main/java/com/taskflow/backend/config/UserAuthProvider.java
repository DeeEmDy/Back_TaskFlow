package com.taskflow.backend.config;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.taskflow.backend.exception.JwtAuthenticationException;
import com.taskflow.backend.services.UserService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class UserAuthProvider {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final Set<String> revokedTokens = new HashSet<>();
    private static final Logger logger = LoggerFactory.getLogger(UserAuthProvider.class);

    // Método para invalidar un token
    public void revokeToken(String token) {
        revokedTokens.add(token);
        logger.info("Token revoked: {}", token);
    }

    // Método para verificar si el token es válido
    public boolean isTokenValid(String token) {
        try {
            return jwtTokenProvider.validateToken(token);
        } catch (Exception e) {
            logger.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    // Método para validar un token
    public Authentication validateToken(String token) {
        if (!isTokenValid(token)) {
            logger.warn("Token is invalid or has been revoked: {}", token);
            throw new JwtAuthenticationException("Invalid token.");
        }
        return jwtTokenProvider.getAuthentication(token);
    }

    // Método para validar el token de refresco
    public Authentication validateRefreshToken(String refreshToken) {
        if (!isTokenValid(refreshToken)) {
            logger.warn("Refresh token is invalid or has been revoked: {}", refreshToken);
            throw new JwtAuthenticationException("Invalid refresh token.");
        }
        return jwtTokenProvider.getAuthentication(refreshToken);
    }

    // Método para crear un nuevo token
    public String createToken(String email) {
        return jwtTokenProvider.createToken(email);
    }

    // Método para verificar si el token ha sido revocado
    public boolean isTokenRevoked(String token) {
        return revokedTokens.contains(token);
    }

}
