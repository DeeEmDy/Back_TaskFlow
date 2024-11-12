package com.taskflow.backend.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BCryptPasswordGenerator {

    // Método para generar la contraseña cifrada
    public static String generateBCryptPassword(String plainPassword) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(plainPassword);
    }

    public static void main(String[] args) {
        // Ejemplo de contraseñas que se van a encriptar
        String[] passwords = {
            "123DylanAD456.",
            "12345678",
            "12345678",
            "12345678"
        };
        
        // Cifrar todas las contraseñas
        for (String password : passwords) {
            String encryptedPassword = generateBCryptPassword(password);
            System.out.println("Contraseña cifrada: " + encryptedPassword);
        }
    }
}
