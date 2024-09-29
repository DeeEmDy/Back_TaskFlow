package com.taskflow.backend.config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.exceptions.JWTVerificationException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class UserAuthFilter extends OncePerRequestFilter {

    private final UserAuthProvider userAuthProvider;
    private static final Logger logger = LoggerFactory.getLogger(UserAuthFilter.class);

    public UserAuthFilter(UserAuthProvider userAuthProvider) {
        this.userAuthProvider = userAuthProvider;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        logger.info("Request URI: {}", requestURI); // Log de URI de solicitud

        if (isPublicURI(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authorizationHeader = request.getHeader("Authorization");
        logger.info("Authorization header: {}", authorizationHeader); // Log del header de autorización

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            logger.info("Token: {}", token); // Log del token

            try {
                Authentication authentication = userAuthProvider.validateToken(token);
                if (authentication != null) {
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.info("Authentication successful for token: {}", token); // Log de éxito
                }
            } catch (JWTVerificationException e) {
                logger.error("Token verification failed: {}", e.getMessage()); // Log de error
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        } else {
            logger.warn("No valid token found in the request."); // Log si no se encuentra un token
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }

    // Método para verificar si la URI es pública
    private boolean isPublicURI(String uri) {
        return uri.startsWith("/auth/login")
                || uri.startsWith("/auth/register")
                || uri.startsWith("/auth/activate");
    }
}

