package com.taskflow.backend.config;


import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordConfig { //Clase para el hasheo de las contraseñas al registrarse.

    @Bean
    public PasswordEncoder passwordEncoder() { //Para consumir este metodo cuando se almacene la contraseña en el registro de usuario.

        return new BCryptPasswordEncoder(); //Uso de la librería para encriptar o hashear nuestra contraseña.
    }
}
