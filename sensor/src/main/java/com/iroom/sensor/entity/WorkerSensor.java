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

	private Double heartRate;
	private Long steps;
	private Double speed;
	private Double pace;
	private Long stepPerMinute;

	@Builder
	public WorkerSensor(Long workerId) {
		this.workerId = workerId;
	}

	public void updateSensor(Double latitude, Double longitude, Double heartRate, Long steps, Double speed, Double pace, Long stepPerMinute) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.heartRate = heartRate;
		this.steps = steps;
		this.speed = speed;
		this.pace = pace;
		this.stepPerMinute = stepPerMinute;
	}
}
