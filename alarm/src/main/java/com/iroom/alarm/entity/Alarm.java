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

	@Column(nullable = false)
	private Long workerId;

	@Column(nullable = false)
	private LocalDateTime occuredAt;

	@Column(nullable = false)
	private String incidentType;

	@Column(nullable = false)
	private Long incidentId;

	@Column(length = 1000)
	private String incidentDescription;

	@PrePersist
	public void prePersist() {
		this.occuredAt = LocalDateTime.now();
	}

	@Builder
	public Alarm(Long workerId, String incidentType, Long incidentId, String incidentDescription) {
		this.workerId = workerId;
		this.incidentType = incidentType;
		this.incidentId = incidentId;
		this.incidentDescription = incidentDescription;
	}
}
