package com.iroom.management.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "WorkerManagement_table")
public class WorkerManagement {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private Long workerId;

    @Temporal(TemporalType.TIMESTAMP)
    private Date enterDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date outDate;
}
