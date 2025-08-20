package com.iroom.modulecommon.dto.event;

public record WorkerSensorEvent(
	Long workerId,
	Double latitude,
	Double longitude,
	Integer age,
	Double heartRate,
	Long steps,
	Double speed,
	Double pace,
	Long stepPerMinute
) {
}