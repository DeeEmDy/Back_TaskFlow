package com.taskflow.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserAuthenticationEntryPoint userAuthenticationEntryPoint;
    private final UserAuthProvider userAuthProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Configuración de CORS
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("http://localhost:5173"); // URL del Frontend
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);

        // Configuración de seguridad
        http
            .cors(cors -> cors.configurationSource(source))
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(userAuthenticationEntryPoint))
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(csrf -> csrf.disable()) // Deshabilitar CSRF ya que estamos usando JWT
            .authorizeHttpRequests(requests -> requests
                // Rutas públicas, disponibles para todos
                .requestMatchers(HttpMethod.GET, "/public/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/login", "/auth/register").permitAll()
                // Rutas protegidas solo para ADMIN
                .requestMatchers("/admin/**").hasAuthority("ADMIN")
                // Rutas protegidas solo para NORMUSER
                .requestMatchers("/user/**").hasAuthority("NORMUSER")
                // Todas las demás rutas requieren autenticación
                .anyRequest().authenticated())
            .addFilterBefore(new UserAuthFilter(userAuthProvider), UsernamePasswordAuthenticationFilter.class); // Filtro de autenticación

        return http.build();
    }
}
