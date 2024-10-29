package com.taskflow.backend.entities;

import java.time.Instant;
import java.time.LocalDate;

import com.taskflow.backend.enums.ProgressTaskCategoriesEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "progress_task")
    private ProgressTaskCategoriesEnum progressTask;

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

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
        this.createdTaskDate = LocalDate.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

}