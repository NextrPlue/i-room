package com.iroom.sensor.dto.WorkerSensor;

public record WorkerUpdateVitalSignsRequest(
	Long workerId,
	Integer heartRate,
	Float bodyTemperature
) {
}
