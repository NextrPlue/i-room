package com.iroom.modulecommon.dto.event;

public record WorkerVitalSignsEvent(
	Long workerId,
	Integer heartRate,
	Float bodyTemperature
) {
}
