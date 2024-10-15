package com.taskflow.backend.config;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.taskflow.backend.dto.UserDto;
import com.taskflow.backend.entities.User;
import com.taskflow.backend.entities.UserPrincipal;
import com.taskflow.backend.exception.TokenValidationException;
import com.taskflow.backend.mappers.UserMapper;
import com.taskflow.backend.services.UserService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class JwtTokenProvider {


    private final UserMapper userMapper;

    @Value("${security.jwt.token.secret-key:your-256-bit-secret}")
    private String secretKey;

    @Value("${security.jwt.token.expiration:3600000}") // 1 hora en milisegundos
    private long expirationTime;

    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    // Método para crear un token JWT
    public String createToken(String email) {
        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + expirationTime);

        return JWT.create()
                .withSubject(email)
                .withIssuedAt(now)
                .withExpiresAt(expiresAt)
                .sign(Algorithm.HMAC256(secretKey));
    }

    // Método para validar el token JWT
    public Authentication getAuthentication(String token) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey)).build();
            DecodedJWT decodedJWT = verifier.verify(token);
    
            if (decodedJWT.getExpiresAt().before(new Date())) {
                throw new TokenValidationException("Token is expired.");
            }
    
            UserDto userDto = userService.findByEmail(decodedJWT.getSubject());
            User user = userMapper.toUser(userDto);

            UserPrincipal userPrincipal = new UserPrincipal(user);
    
            return new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
        } catch (JWTVerificationException | IllegalArgumentException e) {
            logger.error("Token validation error: {}", e.getMessage());
            throw new TokenValidationException("Token is expired or invalid.", e);
        }
    }
    

    // Método para validar un token sin obtener la autenticación
    public boolean validateToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey)).build();
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    // Método para obtener el secret key
    public String getSecretKey() {
        return secretKey;
    }
}
