package com.taskflow.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import static org.springframework.http.HttpMethod.GET;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Dependencias necesarias para la configuración de seguridad
    private final UserAuthenticationEntryPoint userAuthenticationEntryPoint; // Punto de entrada para la autenticación
    private final UserAuthProvider userAuthProvider; // Proveedor de autenticación personalizado
    private final JwtTokenProvider jwtTokenProvider; // Proveedor de token JWT

    // Filtro para la autenticación JWT
    @Bean
    public JwtAuthFilter jwtAuthFilter() {
        return new JwtAuthFilter(jwtTokenProvider, userAuthProvider);
    }

    // Configuración de seguridad para las rutas de la aplicación
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Configuración CORS (Permite solicitudes de orígenes cruzados)
                .cors(cors -> cors.configurationSource(request -> new CorsConfiguration().applyPermitDefaultValues()))
                
                // Desactivación de CSRF (para permitir el uso de JWT)
                .csrf(AbstractHttpConfigurer::disable)
                
                // Configuración para usar JWT en lugar de sesiones tradicionales
                .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Indica que no se utilizarán sesiones

                // Manejo de excepciones de autenticación
                .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(userAuthenticationEntryPoint)) // Configura el punto de entrada para autenticación no autorizada

                // Configuración de las rutas públicas, privadas y compartidas
                .authorizeHttpRequests(authz -> authz
                        // Rutas públicas: accesibles sin autenticación
                        .requestMatchers(GET, "/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll() // Swagger
                        .requestMatchers(HttpMethod.GET, "/public/**", "/auth/activate").permitAll() // Rutas públicas generales
                        .requestMatchers(HttpMethod.POST, "/auth/login", "/auth/register").permitAll() // Login y registro

                        // Rutas privadas: accesibles solo para usuarios con los roles correspondientes
                        .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN") // Solo para administradores
                        .requestMatchers("/user/getAll").hasAnyAuthority("ROLE_ADMIN", "ROLE_NORMUSER") // Administradores y usuarios normales pueden obtener todos los usuarios
                        .requestMatchers("/user/create").hasAuthority("ROLE_ADMIN") // Solo administradores pueden crear usuarios
                        .requestMatchers("/user/**").hasAuthority("ROLE_NORMUSER") // Solo usuarios normales pueden acceder a sus propios datos

                        // Rutas para eliminar y actualizar: accesibles para usuarios normales y administradores
                        .requestMatchers(HttpMethod.DELETE, "/auth/logout", "/user/delete/{id}").hasAnyAuthority("ROLE_NORMUSER", "ROLE_ADMIN") // Logout y eliminación de usuarios
                        .requestMatchers(HttpMethod.PUT, "/user/update/{id}").hasAnyAuthority("ROLE_NORMUSER", "ROLE_ADMIN") // Actualización de usuarios

                        // Otras rutas requieren autenticación
                        .anyRequest().authenticated() // Requiere autenticación para cualquier otra ruta

                )
                // Añade el filtro JWT antes del filtro estándar de autenticación
                .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class);

        // Devuelve la configuración de seguridad construida
        return http.build();
    }
}
