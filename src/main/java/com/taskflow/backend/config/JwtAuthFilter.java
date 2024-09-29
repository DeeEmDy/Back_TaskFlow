package com.taskflow.backend.config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor; //Ya que queremos que este filtro se utilice: 1 vez por solicitud.

@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final UserAuthProvider userAuthProvider;
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);


    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String headerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        logger.info("Authorization header: {}", headerToken); // Log del header de autorización

        if (headerToken != null && headerToken.startsWith("Bearer ")) {
            String token = headerToken.substring(7);
            logger.info("Token: {}", token); // Log del token

            try {
                Authentication auth = userAuthProvider.validateToken(token);
                SecurityContextHolder.getContext().setAuthentication(auth);
                logger.info("Authentication successful for token: {}", token); // Log de éxito
            } catch (RuntimeException e) {
                logger.error("Token validation failed: {}", e.getMessage()); // Log de error
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // Respuesta 401
                return;
            }
        } else {
            logger.warn("No valid token found in the request."); // Log si no se encuentra un token
        }

        filterChain.doFilter(request, response);
    }
}
