package com.iroom.management.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "WorkerManagement")
public class WorkerManagement {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private Long workerId;

    private LocalDateTime enterDate;

    private LocalDateTime outDate;

    @PrePersist
    protected void onCreate() {
        this.enterDate = LocalDateTime.now();
    }

    public void markExitedNow(){
        this.outDate = LocalDateTime.now();
    }

    @Builder
    public WorkerManagement(Long workerId){
        this.workerId = workerId;
    }
}
