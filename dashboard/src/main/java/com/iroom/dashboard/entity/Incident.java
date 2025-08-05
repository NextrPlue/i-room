package com.iroom.dashboard.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Incident {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private Long workerId;

	@Column(nullable = false)
	private LocalDateTime occurredAt;

	@Column(nullable = false)
	private String incidentType;

	//근로자 위치
	private Double latitude;
	private Double longitude;

	@Column(length = 1000)
	private String incidentDescription;
}
