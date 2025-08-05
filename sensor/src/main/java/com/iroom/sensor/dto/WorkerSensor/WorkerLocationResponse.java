package com.iroom.sensor.dto.WorkerSensor;

import com.iroom.sensor.entity.WorkerSensor;

public record WorkerLocationResponse(
	Long workerId,
	Double latitude,
	Double longitude
) {
	public WorkerLocationResponse(WorkerSensor entity) {
		this(
			entity.getWorkerId(),
			entity.getLatitude(),
			entity.getLongitude()
		);
	}
}
