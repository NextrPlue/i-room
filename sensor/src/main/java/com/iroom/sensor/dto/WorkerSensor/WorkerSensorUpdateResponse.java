package com.iroom.sensor.dto.WorkerSensor;

import com.iroom.sensor.entity.WorkerSensor;

public record WorkerSensorUpdateResponse(
	Long workerId,
	Double latitude,
	Double longitude,
	Integer heartRate
) {
	public WorkerSensorUpdateResponse(WorkerSensor workerSensor) {
		this(
			workerSensor.getWorkerId(),
			workerSensor.getLatitude(),
			workerSensor.getLongitude(),
			workerSensor.getHeartRate()
		);
	}
}