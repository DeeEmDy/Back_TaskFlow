package com.taskflow.backend.config;

import java.io.IOException;

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

    public UserAuthFilter(UserAuthProvider userAuthProvider) {
        this.userAuthProvider = userAuthProvider;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Obtén la URI de la solicitud
        String requestURI = request.getRequestURI();

        // Verifica si la URI corresponde a una ruta pública
        if (requestURI.startsWith("/auth/login")
                || requestURI.startsWith("/auth/register")
                || requestURI.startsWith("/auth/activate")) {
            // No aplicar el filtro de autenticación a estas rutas
            filterChain.doFilter(request, response);
            return;
        }

        // Procesa el token JWT para rutas protegidas
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            try {
                token = token.substring(7); // Remove "Bearer " prefix
                Authentication authentication = userAuthProvider.validateToken(token);

                if (authentication != null) {
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (JWTVerificationException e) {
                // Token is invalid or expired
                SecurityContextHolder.clearContext();
            }
        }

        // Continua con la cadena de filtros
        filterChain.doFilter(request, response);
    }
}
