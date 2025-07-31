package com.iroom.sensor.dto.WorkerHealth;

public record WorkerUpdateLocationRequest(
	Long workerId,
	Double latitude,
	Double longitude
) {
}
