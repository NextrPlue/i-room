package com.iroom.management.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "WorkerManagement_table")
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
}
