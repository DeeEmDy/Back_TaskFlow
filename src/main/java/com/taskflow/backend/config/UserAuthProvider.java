package com.taskflow.backend.config;


import java.util.Base64;
import java.util.Collections;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.taskflow.backend.dto.UserDto;
import com.taskflow.backend.exception.JwtAuthenticationException;
import com.taskflow.backend.services.UserService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class UserAuthProvider {

    @Value("${security.jwt.token.secret-key:secret-value}")
    private String secretKey;

    @Value("${security.jwt.token.expiration:3600000}") // 1 hora en milisegundos
    private long expirationTime;

    private final UserService userService;

    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes()); // Encriptar la clave secreta en base64
    }

    // Método para crear un token JWT
    public String createToken(String email) {
        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + expirationTime);
        return JWT.create()
                .withIssuer(email)
                .withIssuedAt(now)
                .withExpiresAt(expiresAt)
                .sign(Algorithm.HMAC256(secretKey));
    }    

    // Método para validar el token JWT
    public Authentication validateToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey))
                    .build();
            DecodedJWT decodedJWT = verifier.verify(token);
            UserDto user = userService.findByEmail(decodedJWT.getIssuer());
            return new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
        } catch (JWTVerificationException | IllegalArgumentException e) {
            throw new JwtAuthenticationException("Invalid or expired token", e);
        }
    }    

    // Método para crear un token de refrescamiento
    public String createRefreshToken(String email) {
        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + expirationTime * 2); // Token de refresco tiene una duración más larga
    
        return JWT.create()
                .withIssuer(email)
                .withIssuedAt(now)
                .withExpiresAt(expiresAt)
                .sign(Algorithm.HMAC256(secretKey));
    }
    
    // Método para validar el token de refrescamiento.
    public Authentication validateRefreshToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey))
                    .build();
    
            DecodedJWT decodedJWT = verifier.verify(token);
    
            UserDto user = userService.findByEmail(decodedJWT.getIssuer());
    
            return new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
        } catch (JWTVerificationException | IllegalArgumentException e) {
            throw new JwtAuthenticationException("Invalid or expired refresh token", e);
        }
    }
    
}