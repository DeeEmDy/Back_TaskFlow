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

    private final UserAuthenticationEntryPoint userAuthenticationEntryPoint;
    private final UserAuthProvider userAuthProvider;
    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public JwtAuthFilter jwtAuthFilter() {
        return new JwtAuthFilter(jwtTokenProvider, userAuthProvider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("http://localhost:5173");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);

        http
                .cors(cors -> cors.configurationSource(source))
                .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(userAuthenticationEntryPoint))
                .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authz -> authz
                // Rutas públicas
                .requestMatchers(HttpMethod.GET, "/public/**", "/auth/activate").permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/login", "/auth/register").permitAll()
                
                // Rutas de administración
                .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                
                // Rutas de usuario
                .requestMatchers("/user/getAll").hasAnyAuthority("ROLE_ADMIN", "ROLE_NORMUSER")
                .requestMatchers("/user/create").hasAuthority("ROLE_ADMIN")
                .requestMatchers("/user/**").hasAuthority("ROLE_NORMUSER")
                .requestMatchers(HttpMethod.DELETE, "/auth/logout", "/user/delete/{id}").hasAnyAuthority("ROLE_NORMUSER", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/user/update/{id}").hasAnyAuthority("ROLE_NORMUSER", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.GET, "/user/getById/{id}").hasAnyAuthority("ROLE_NORMUSER", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/user/update-password").hasAnyAuthority("ROLE_NORMUSER", "ROLE_ADMIN")

                // Rutas de Task
                .requestMatchers(HttpMethod.GET, "/task/getAll").hasAnyAuthority("ROLE_NORMUSER", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.POST, "/task/create").hasAnyAuthority("ROLE_NORMUSER", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.GET, "/task/getById/{id}").hasAnyAuthority("ROLE_NORMUSER", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/task/update/{id}").hasAnyAuthority("ROLE_NORMUSER", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/task/delete/{id}").hasAnyAuthority("ROLE_NORMUSER", "ROLE_ADMIN")

                .anyRequest().authenticated())
                .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers(HttpMethod.OPTIONS, "/**");
    }
}
