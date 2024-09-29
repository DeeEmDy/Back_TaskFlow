package com.taskflow.backend.entities;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data

@Getter
@Setter
@Entity
@Table(name = "tbuser")

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tbuser_id_gen")
    @SequenceGenerator(name = "tbuser_id_gen", sequenceName = "tbuser_id_user_seq", allocationSize = 1)
    @Column(name = "id_user", nullable = false)
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "first_surname")
    private String firstSurname;

    @Column(name = "second_surname")
    private String secondSurname;

    @Column(name = "id_card")
    private String idCard;

    @Column(name = "phone_number")
    private String phoneNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_image")
    private Image idImage;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_rol")
    private Rol role; 

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "user_verified")
    private Boolean userVerified;

    @Column(name = "status")
    private Boolean status;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "activation_token")
    private String activationToken;

    @Column(name = "activation_token_expiration")
    private Instant activationTokenExpiration;

    //Metodos de la clase
    public void setActivationToken(String token) {
        this.activationToken = token;
    }
    
    public void setActivationTokenExpiration(Instant expiration) {
        this.activationTokenExpiration = expiration;
    }
    
    public void clearActivationToken() {
        this.activationToken = null;
        this.activationTokenExpiration = null;
    }
}