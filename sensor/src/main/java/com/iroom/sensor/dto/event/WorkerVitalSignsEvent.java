package com.iroom.sensor.dto.event;

public record WorkerVitalSignsEvent(
	Long workerId,
	Integer heartRate,
	Float bodyTemperature
) {
}
