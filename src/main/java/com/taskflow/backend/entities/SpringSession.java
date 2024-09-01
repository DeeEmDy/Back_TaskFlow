package com.taskflow.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "spring_session")
public class SpringSession {
    @Id
    @SequenceGenerator(name = "spring_session_id_gen", sequenceName = "tbuser_id_user_seq", allocationSize = 1)
    @Column(name = "primary_id", nullable = false)
    private String primaryId;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "creation_time")
    private Long creationTime;

    @Column(name = "last_access_time")
    private Long lastAccessTime;

    @Column(name = "max_inactive_interval")
    private Integer maxInactiveInterval;

    @Column(name = "expiry_time")
    private Long expiryTime;

    @Column(name = "principal_name")
    private String principalName;

}