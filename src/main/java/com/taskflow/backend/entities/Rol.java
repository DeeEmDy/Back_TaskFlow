package com.taskflow.backend.entities;

import java.time.Instant;

import com.taskflow.backend.enums.RoleTypeEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "rol_name") // Valores del enum RoleTypeEnum
    private RoleTypeEnum rolName;

    @Column(name = "status")
    private Boolean status;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

}