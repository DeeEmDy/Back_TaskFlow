package com.taskflow.backend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;


@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data

@Table(name = "tbuser")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //Para indicarle que el ID es autoincrementable a nivel de base de datos.
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "first_surname")
    private String first_surname;

    @Column(name = "second_surname")
    private String second_surname;

    @Column(name = "id_card")
    private String id_card;

    @Column(name = "phone_number")
    private String phone_number;

    @Column(name = "id_image")
    private int id_image;

    //@Column(name = "email")
    @Column(nullable = false)
    private String email;

    //@Column(name = "password")
    @Column(nullable = false)
    private String password;

    @Column(name = "user_verified")
    private boolean user_verified;

    @Column(name = "status")
    private boolean status;      //Estado del registro de usuario:  0:Inactivo           1:Activo         pddt: Para manejar borrados logicos.

    @Column(name = "expiration_date")
    private Date expiration_date;
}
