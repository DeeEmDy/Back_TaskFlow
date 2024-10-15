package com.taskflow.backend.entities;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserPrincipal implements UserDetails {

    private final User user;
    private static final Logger logger = LoggerFactory.getLogger(UserPrincipal.class);

    public UserPrincipal(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();

        // Asegúrate de que el rol tenga el prefijo "ROLE_"
        if (user.getRole() != null) {
            authorities.add(new SimpleGrantedAuthority(user.getRole().getRolName().name()));
        }        
        // Log de depuración
        if (logger.isInfoEnabled()) {
            logger.info("Roles del usuario obtenidos: {}", authorities);
        }
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Aquí podrías agregar lógica según el estado de la cuenta
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Aquí podrías agregar lógica para bloquear cuentas
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Lógica para credenciales expiradas si es necesario
    }

    @Override
    public boolean isEnabled() {
        return user.getStatus() != null && user.getStatus(); //Mediante el campo 'status' habilitamos o deshabilitamos las cuentas de los usuarios.
    }
}
