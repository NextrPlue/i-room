package com.iroom.user.entity;

import com.iroom.user.enums.WorkerRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name="Workers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Worker {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;
    private String email;
    private String password;
    private String phone;

    @Enumerated(EnumType.STRING)
    private WorkerRole role;

    private String bloodType;
    private String gender;
    private Integer age;
    private Float weight;
    private Float height;
    private String jobTitle; // 직책(팀장, 반장 등)
    private String occupation; // 직종(철근공, 목공 등)
    private String department; // 부서
    private String faceImageUrl;
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
    public Worker(String name, String email, String password, String phone, WorkerRole role, String bloodType, String gender, Integer age, Float weight, Float height, String jobTitle, String occupation, String department, String faceImageUrl) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.role = role;
        this.bloodType = bloodType;
        this.gender = gender;
        this.age = age;
        this.weight = weight;
        this.height = height;
        this.jobTitle = jobTitle;
        this.occupation = occupation;
        this.department = department;
        this.faceImageUrl = faceImageUrl;
    }

    public void updateInfo(String name, String email, String password, String phone, String bloodType, String gender, Integer age, Float weight, Float height, String jobTitle, String occupation, String department, String faceImageUrl) {        this.name = name;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.bloodType = bloodType;
        this.gender = gender;
        this.age = age;
        this.weight = weight;
        this.height = height;
        this.jobTitle = jobTitle;
        this.occupation = occupation;
        this.department = department;
        this.faceImageUrl = faceImageUrl;
    }

    public void updatePassword(String password) {
        this.password = password;
    }
}
