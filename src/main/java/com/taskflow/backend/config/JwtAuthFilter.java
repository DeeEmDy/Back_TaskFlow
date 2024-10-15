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

    private final JwtTokenProvider jwtTokenProvider;
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String token = extractToken(request);
        logger.info("Processing request to {}", request.getRequestURI());
        logger.info("Authorization header from IP {}: {}", request.getRemoteAddr(), request.getHeader(HttpHeaders.AUTHORIZATION));

        if (token != null) {
            logger.info("Extracted token: {}", token);

            try {
                Authentication auth = jwtTokenProvider.getAuthentication(token);
                if (auth != null) {
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    logger.info("Authentication successful for token: {}", token);
                    logger.info("User {} authenticated with authorities: {}", auth.getName(), auth.getAuthorities());
                } else {
                    logger.warn("Authentication failed for token: {}", token);
                    SecurityContextHolder.clearContext();
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                    return;
                }
            } catch (TokenValidationException e) {
                logger.error("Token validation failed: {}", e.getMessage());
                SecurityContextHolder.clearContext();
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                return;
            }
        } else {
            logger.warn("No valid token found in the request from IP: {}", request.getRemoteAddr());
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String headerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (headerToken != null && headerToken.startsWith("Bearer ")) {
            return headerToken.substring(7);
        }
        return null;
    }
}