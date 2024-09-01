package com.taskflow.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "tbreport")
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tbreport_id_gen")
    @SequenceGenerator(name = "tbreport_id_gen", sequenceName = "tbreport_idreport_seq", allocationSize = 1)
    @Column(name = "idreport", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user")
    private com.taskflow.backend.entities.User idUser;

    @Column(name = "cut_off_task_date")
    private LocalDate cutOffTaskDate;

    @Column(name = "amount_task_assigned")
    private Integer amountTaskAssigned;

    @Column(name = "total_task_achieved")
    private Integer totalTaskAchieved;

    @Column(name = "total_task_failed")
    private Integer totalTaskFailed;

    @Column(name = "average_total_task_ownled")
    private Double averageTotalTaskOwnled;

    @Column(name = "total_points_owned")
    private Integer totalPointsOwned;

    @Column(name = "status")
    private Boolean status;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

}