package com.iroom.sensor.dto.WorkerHealth;

import com.iroom.sensor.entity.WorkerHealth;

public record WorkerUpdateLocationResponse(
	Long workerId,
	Double latitude,
	Double longitude
) {
	// 엔티티 → DTO 변환 생성자
	public WorkerUpdateLocationResponse(WorkerHealth entity) {
		this(
			entity.getWorkerId(),
			entity.getLatitude(),
			entity.getLongitude()
		);
	}
}
