package com.iroom.sensor.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "WorkerSensors")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkerSensor {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long workerId;

	private Double latitude;
	private Double longitude;

	private Integer heartRate;

	@Builder
	public WorkerSensor(Long workerId) {
		this.workerId = workerId;
	}

	public void updateSensor(Double latitude, Double longitude, Integer heartRate) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.heartRate = heartRate;
	}
}
