package com.iroom.sensor.dto.WorkerSensor;

public record WorkerUpdateVitalSignsResponse(
	Long workerId,
	Integer heartRate,
	Float bodyTemperature
) {
}
