package com.iroom.alarm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "Alarm")
public class Alarm {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private Long workerId;

    @Column(nullable = false)
    private LocalDateTime occuredAt;

    @Column(nullable = false)
    private String incidentType;

    @Column(nullable = false)
    private Long incidentId;

    @Column(length = 1000)
    private String incidentDescription;

    @PrePersist
    public void prePersist() {
        this.occuredAt = LocalDateTime.now();
    }

    @Builder
    public Alarm(Long workerId, String incidentType, Long incidentId, String incidentDescription) {
        this.workerId = workerId;
        this.incidentType = incidentType;
        this.incidentId = incidentId;
        this.incidentDescription = incidentDescription;
    }
}
