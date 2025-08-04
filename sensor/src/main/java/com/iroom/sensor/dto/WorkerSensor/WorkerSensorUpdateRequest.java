package com.iroom.sensor.dto.WorkerSensor;

public record WorkerSensorUpdateRequest(
	Long workerId,
	Double latitude,
	Double longitude,
	Integer heartRate
) {
}