package com.iroom.dashboard.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "Blueprint_table")
public class Blueprint {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String blueprintUrl;

    @Column(nullable = false)
    private Integer floor;

    @Column(nullable = false)
    private Double width;

    @Column(nullable = false)
    private Double height;
}
