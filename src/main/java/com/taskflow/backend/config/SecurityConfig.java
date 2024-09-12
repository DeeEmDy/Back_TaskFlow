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
        config.addAllowedOrigin("http://localhost:5173"); // URL comunicación FrontEnd
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);

        // Configuración de seguridad
        http
            .cors(cors -> cors.configurationSource(source))
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(userAuthenticationEntryPoint)
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .csrf(csrf -> csrf
                .disable() // Deshabilitar CSRF ya que estamos usando JWT
            )
            .authorizeHttpRequests(requests -> requests
                .requestMatchers(HttpMethod.POST, "/login", "/register").permitAll() // Permitir acceso sin autenticación a login y register
                .requestMatchers(HttpMethod.GET, "/public/**").permitAll() // Ejemplo: permitir acceso sin autenticación a rutas públicas
                .anyRequest().authenticated() // Requerir autenticación para cualquier otra solicitud
            )
            .addFilterBefore(new UserAuthFilter(userAuthProvider), UsernamePasswordAuthenticationFilter.class); // Agregar filtro de autenticación

        return http.build();
    }
}
