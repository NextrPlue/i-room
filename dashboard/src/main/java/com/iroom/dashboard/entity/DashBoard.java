package com.iroom.dashboard.entity;



import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;



@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashBoard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private  Long id;

    @Column
    private String metricType;

    @Column
    private Integer metricValue;

    @Column
    private LocalDate recordedAt;

    @PrePersist
    protected void onCreate() {
        this.recordedAt = LocalDate.now();
    }


    public void updateMetricValue(){
        this.metricValue+=1;
    }
}