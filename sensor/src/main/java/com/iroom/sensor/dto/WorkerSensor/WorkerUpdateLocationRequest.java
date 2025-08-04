package com.iroom.sensor.dto.WorkerSensor;

public record WorkerUpdateLocationRequest(
	Long workerId,
	Double latitude,
	Double longitude
) {
}
