package com.iroom.user.system.entity;

import com.iroom.user.system.enums.SystemRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name="Systems")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class System {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String apiKey;

    @Enumerated(EnumType.STRING)
    private SystemRole role;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Builder
    public System(String name, String apiKey, SystemRole role) {
        this.name = name;
        this.apiKey = apiKey;
        this.role = role;
    }
}
