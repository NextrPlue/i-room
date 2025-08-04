package com.iroom.sensor.dto.WorkerSensor;

import com.iroom.sensor.entity.WorkerSensor;

public record WorkerUpdateLocationResponse(
	Long workerId,
	Double latitude,
	Double longitude
) {
	// 엔티티 → DTO 변환 생성자
	public WorkerUpdateLocationResponse(WorkerSensor entity) {
		this(
			entity.getWorkerId(),
			entity.getLatitude(),
			entity.getLongitude()
		);
	}
}
