package com.iroom.sensor.dto.WorkerSensor;

public record WorkerSensorUpdateRequest(
	Double latitude,
	Double longitude,
	Integer heartRate
) {
}