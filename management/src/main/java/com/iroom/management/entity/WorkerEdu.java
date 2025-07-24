package com.iroom.management.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "WorkerEdu")
public class WorkerEdu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long workerId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String certUrl;

    @Column(nullable = false)
    private LocalDate eduDate;

    @Builder
    public WorkerEdu(Long workerId, String name, String certUrl, LocalDate eduDate) {
        this.workerId = workerId;
        this.name = name;
        this.certUrl = certUrl;
        this.eduDate = eduDate;
    }
}
