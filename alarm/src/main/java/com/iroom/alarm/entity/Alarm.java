package com.iroom.alarm.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "Alarm")
public class Alarm {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private Long workerId;

	@Column(nullable = false)
	private LocalDateTime occurredAt;

	@Column(nullable = false)
	private String incidentType;

	@Column(nullable = false)
	private Long incidentId;

	private String latitude;
	private String longitude;

	@Column(length = 1000)
	private String incidentDescription;

	private String imageUrl;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	@PrePersist
	public void prePersist() {
		this.createdAt = LocalDateTime.now();
	}

	@Builder
	public Alarm(Long workerId, LocalDateTime occurredAt, String incidentType, Long incidentId, String latitude,
		String longitude, String incidentDescription, String imageUrl) {
		this.workerId = workerId;
		this.occurredAt = occurredAt;
		this.incidentType = incidentType;
		this.incidentId = incidentId;
		this.latitude = latitude;
		this.longitude = longitude;
		this.incidentDescription = incidentDescription;
		this.imageUrl = imageUrl;
	}
}
