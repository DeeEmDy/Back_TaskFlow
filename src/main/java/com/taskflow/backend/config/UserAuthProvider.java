package com.taskflow.backend.config;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class UserAuthProvider {

    // Cambia la clave secreta aquí
    @Value("${security.jwt.token.secret-key:your-256-bit-secret}")
    private String secretKey;

    @Value("${security.jwt.token.expiration:3600000}") // 1 hora en milisegundos
    private long expirationTime;

    private final UserService userService;

    // Set para almacenar tokens revocados
    private final Set<String> revokedTokens = new HashSet<>(); 

    private static final Logger logger = LoggerFactory.getLogger(UserAuthProvider.class);

    // Método para crear un token JWT
    public String createToken(String email) {
        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + expirationTime);
        String token = JWT.create()
                .withSubject(email) 
                .withIssuedAt(now)
                .withExpiresAt(expiresAt)
                .sign(Algorithm.HMAC256(secretKey)); 
        logger.info("Token creado para el usuario: {}", email);
        return token;
    }

    // Método para validar el token JWT
    public Authentication validateToken(String token) {
        if (isTokenRevoked(token)) { 
            logger.warn("Token revocado: {}", token);
            throw new JwtAuthenticationException("Token has been revoked");
        }
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey)).build();
            DecodedJWT decodedJWT = verifier.verify(token);
            UserDto user = userService.findByEmail(decodedJWT.getSubject());
            logger.info("Token validado con éxito para el usuario: {}", decodedJWT.getSubject());
            return new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
        } catch (JWTVerificationException | IllegalArgumentException e) {
            logger.error("Error en la validación del token: {}", e.getMessage());
            throw new JwtAuthenticationException("Token expirado o inválido.", e);
        }
    }    

    // Método para crear un token de refrescamiento
    public String createRefreshToken(String email) {
        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + expirationTime * 2); 
        String refreshToken = JWT.create()
                .withSubject(email)
                .withIssuedAt(now)
                .withExpiresAt(expiresAt)
                .sign(Algorithm.HMAC256(secretKey));
        logger.info("Token de refresco creado para el usuario: {}", email);
        return refreshToken;
    }
    
    // Método para validar el token de refresco
    public Authentication validateRefreshToken(String token) {
        if (isTokenRevoked(token)) {
            logger.warn("Token de refresco revocado: {}", token);
            throw new JwtAuthenticationException("El token de refresco ha sido revocado.");
        }
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey)).build();
            DecodedJWT decodedJWT = verifier.verify(token);
            UserDto user = userService.findByEmail(decodedJWT.getSubject());
            logger.info("Token de refresco validado con éxito para el usuario: {}", decodedJWT.getSubject());
            return new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
        } catch (JWTVerificationException | IllegalArgumentException e) {
            logger.error("Error en la validación del token de refresco: {}", e.getMessage());
            throw new JwtAuthenticationException("Refresh token expirado o inválido.", e);
        }
    }
    
    // Método para invalidar un token
    public void invalidateToken(String token) {
        revokedTokens.add(token); // Revocar el token para futuras solicitudes.
        logger.info("Token invalidado: {}", token);
    }

    // Método para obtener el token de autenticación de un objeto Authentication
    public String getTokenFromAuth(Authentication authentication) {
        UserDto user = (UserDto) authentication.getPrincipal();
        String token = createToken(user.getEmail());
        logger.info("Token obtenido para el usuario: {}", user.getEmail());
        return token;
    }

    // Método auxiliar para verificar si un token ha sido revocado
    private boolean isTokenRevoked(String token) {
        return revokedTokens.contains(token);
    }
}
