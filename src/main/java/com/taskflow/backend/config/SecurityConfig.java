package com.taskflow.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
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

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_NORMUSER = "NORMUSER";

    private final UserAuthenticationEntryPoint userAuthenticationEntryPoint;
    private final JwtTokenProvider jwtTokenProvider; //Bean del JwtTokenProvider para la inyección de dependencias a los usuarios.

    @Bean
    public JwtAuthFilter jwtAuthFilter() {
        return new JwtAuthFilter(jwtTokenProvider); //Creación del filtro de autenticación con el JwtTokenProvider.
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Configuración de CORS
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("http://localhost:5173");
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
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authz -> authz
                .requestMatchers(HttpMethod.GET, "/public/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/login", "/auth/register", "/auth/activate").permitAll()
                .requestMatchers("/admin/**").hasRole(ROLE_ADMIN) 
                .requestMatchers("/user/**").hasRole(ROLE_NORMUSER)
                .requestMatchers(HttpMethod.GET, "/user/getAll").hasAnyRole(ROLE_ADMIN, ROLE_NORMUSER)
                .requestMatchers(HttpMethod.DELETE, "/auth/logout").hasAnyRole(ROLE_NORMUSER, ROLE_ADMIN)
                .anyRequest().authenticated())
            .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class); // Usar el método para obtener el filtro

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers(HttpMethod.OPTIONS, "/**");
    }
}
