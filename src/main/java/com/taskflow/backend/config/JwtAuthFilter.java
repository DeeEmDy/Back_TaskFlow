package com.taskflow.backend.config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.taskflow.backend.exception.TokenValidationException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

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
        logger.info("Authorization header from IP {}: {}", request.getRemoteAddr(), headerToken);

        if (headerToken != null && headerToken.startsWith("Bearer ")) {
            String token = headerToken.substring(7);
            logger.info("Extracted token: {}", token);

            // Dentro de JwtAuthFilter.java
            try {
                Authentication auth = userAuthProvider.validateToken(token);
                SecurityContextHolder.getContext().setAuthentication(auth);
                logger.info("Authentication successful for token: {}", token);
            } catch (TokenValidationException e) { // Cambia a la nueva excepción
                logger.error("Token validation failed: {}", e.getMessage());
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Unauthorized: " + e.getMessage()); // Mensaje adicional
                return;
            }

        } else {
            logger.warn("No valid token found in the request from IP: {}", request.getRemoteAddr());
        }

        filterChain.doFilter(request, response);
    }
}
