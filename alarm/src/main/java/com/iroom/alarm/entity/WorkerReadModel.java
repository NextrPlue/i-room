package com.iroom.alarm.entity;

import java.time.LocalDateTime;

import com.iroom.modulecommon.dto.event.WorkerEvent;
import com.iroom.modulecommon.enums.BloodType;
import com.iroom.modulecommon.enums.Gender;
import com.iroom.modulecommon.enums.WorkerRole;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "WorkerReadModel")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkerReadModel {
	@Id
	private Long id;

	private String name;
	private String email;
	private String phone;

	@Enumerated(EnumType.STRING)
	private WorkerRole role;

	@Enumerated(EnumType.STRING)
	private BloodType bloodType;

	@Enumerated(EnumType.STRING)
	private Gender gender;

	private Integer age;
	private Float weight;
	private Float height;
	private String jobTitle;
	private String occupation;
	private String department;
	private String faceImageUrl;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	@Builder
	public WorkerReadModel(Long id, String name, String email, String phone, WorkerRole role,
		BloodType bloodType, Gender gender, Integer age, Float weight, Float height,
		String jobTitle, String occupation, String department, String faceImageUrl, LocalDateTime createdAt,
		LocalDateTime updatedAt) {
		this.id = id;
		this.name = name;
		this.email = email;
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
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public void updateFromEvent(WorkerEvent event) {
		this.name = event.name();
		this.email = event.email();
		this.phone = event.phone();
		this.role = event.role();
		this.bloodType = event.bloodType();
		this.gender = event.gender();
		this.age = event.age();
		this.weight = event.weight();
		this.height = event.height();
		this.jobTitle = event.jobTitle();
		this.occupation = event.occupation();
		this.department = event.department();
		this.faceImageUrl = event.faceImageUrl();
		this.createdAt = event.createdAt();
		this.updatedAt = event.updatedAt();
	}
}