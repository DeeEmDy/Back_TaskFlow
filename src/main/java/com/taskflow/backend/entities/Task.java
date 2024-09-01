package com.taskflow.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "tbtask")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tbtask_id_gen")
    @SequenceGenerator(name = "tbtask_id_gen", sequenceName = "tbtask_idtask_seq", allocationSize = 1)
    @Column(name = "idtask", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user")
    private com.taskflow.backend.entities.User idUser;

    @Column(name = "title")
    private String title;

    @Column(name = "description_task")
    private String descriptionTask;

    @Column(name = "created_task_date")
    private LocalDate createdTaskDate;

    @Column(name = "expiration_task_date")
    private LocalDate expirationTaskDate;

    @Column(name = "progress_task")
    private Boolean progressTask;

    @Column(name = "finalization_task_date")
    private LocalDate finalizationTaskDate;

    @Column(name = "score")
    private Integer score;

    @Column(name = "status")
    private Boolean status;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

}