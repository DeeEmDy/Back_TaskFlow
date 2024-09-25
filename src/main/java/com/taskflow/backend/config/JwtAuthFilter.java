package com.taskflow.backend.config;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse; //Ya que queremos que este filtro se utilice: 1 vez por solicitud.
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final UserAuthProvider userAuthProvider;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String headerToken = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (headerToken != null && headerToken.startsWith("Bearer ")) {
            String token = headerToken.substring(7);
            try {
                SecurityContextHolder.getContext().setAuthentication(userAuthProvider.validateToken(token));
            } catch (RuntimeException e) {
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // Respuesta 401
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
