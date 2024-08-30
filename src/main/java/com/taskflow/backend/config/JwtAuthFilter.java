package com.taskflow.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter; //Ya que queremos que este filtro se utilice: 1 vez por solicitud.

import java.io.IOException;


@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final UserAuthProvider userAuthProvider;


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

            String headerToken  = request.getHeader(HttpHeaders.AUTHORIZATION); //Se comprueba el encabezado de autorización del JWT Token.

            if (headerToken != null) {

                String[] tokens = headerToken.split(" ");

                if (tokens.length == 2 && "Bearer".equals(tokens[0])) {

                    try{
                        SecurityContextHolder.getContext().setAuthentication(
                                userAuthProvider.validateToken(tokens[1])
                        );
                    } catch (RuntimeException e) { //Si algo sale mal durante la autentificación, borrar el contexto de seguridad y arrojar error.
                        SecurityContextHolder.clearContext();
                        throw e;
                    }
                }
            }
            filterChain.doFilter(request, response); //Llamar al metodo de filtrado doFilter.
    }
}
