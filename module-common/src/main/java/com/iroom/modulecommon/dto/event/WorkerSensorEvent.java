package com.iroom.modulecommon.dto.event;

public record WorkerSensorEvent(
	Long workerId,
	Double latitude,
	Double longitude,
	Integer heartRate
) {
}