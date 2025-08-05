package com.iroom.sensor.dto.WorkerSensor;

public record WorkerSensorUpdateRequest(
	Double latitude,
	Double longitude,
	Double heartRate,
	Long steps,
	Double speed,
	Double pace,
	Long stepPerMinute
) {
}