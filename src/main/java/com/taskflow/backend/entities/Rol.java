package com.taskflow.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "tbrol")
public class Rol {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tbrol_id_gen")
    @SequenceGenerator(name = "tbrol_id_gen", sequenceName = "tbrol_idrol_seq", allocationSize = 1)
    @Column(name = "idrol", nullable = false)
    private Integer id;

    @Column(name = "rol_name")
    private String rolName;

    @Column(name = "status")
    private Boolean status;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

}