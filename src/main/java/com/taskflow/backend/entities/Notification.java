package com.taskflow.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "tbnotification")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tbnotification_id_gen")
    @SequenceGenerator(name = "tbnotification_id_gen", sequenceName = "tbnotification_idnotification_seq", allocationSize = 1)
    @Column(name = "idnotification", nullable = false)
    private Integer id;

    @Column(name = "notification_message")
    private String notificationMessage;

    @Column(name = "read")
    private Boolean read;

    @Column(name = "dispatch_day")
    private Instant dispatchDay;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user")
    private com.taskflow.backend.entities.User idUser;

    @Column(name = "status")
    private Boolean status;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

}