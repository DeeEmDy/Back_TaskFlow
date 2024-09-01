package com.taskflow.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "tbimage")
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tbimage_id_gen")
    @SequenceGenerator(name = "tbimage_id_gen", sequenceName = "tbimage_idimage_seq", allocationSize = 1)
    @Column(name = "idimage", nullable = false)
    private Integer id;

    @Column(name = "image_content", length = Integer.MAX_VALUE)
    private String imageContent;

    @Column(name = "status")
    private Boolean status;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

}