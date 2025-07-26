package com.iroom.dashboard.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "Blueprint")
public class Blueprint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String blueprintUrl;

    @Column(nullable = false)
    private Integer floor;

    @Column(nullable = false)
    private Double width;

    @Column(nullable = false)
    private Double height;

    @Builder
    public Blueprint (String blueprintUrl, Integer floor, Double width, Double height) {
        this.blueprintUrl =  blueprintUrl;
        this.floor = floor;
        this.width = width;
        this.height = height;
    }

    public void update(String blueprintUrl, Integer floor, Double width, Double height) {
        this.blueprintUrl = blueprintUrl;
        this.floor = floor;
        this.width = width;
        this.height = height;
    }
}
