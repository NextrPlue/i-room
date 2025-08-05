package com.iroom.sensor.dto.WorkerSensor;

import com.iroom.sensor.entity.WorkerSensor;

public record WorkerSensorUpdateResponse(
	Long workerId,
	Double latitude,
	Double longitude,
	Double heartRate,
	Long steps,
	Double speed,
	Double pace,
	Long stepPerMinute
) {
	public WorkerSensorUpdateResponse(WorkerSensor workerSensor) {
		this(
			workerSensor.getWorkerId(),
			workerSensor.getLatitude(),
			workerSensor.getLongitude(),
			workerSensor.getHeartRate(),
			workerSensor.getSteps(),
			workerSensor.getSpeed(),
			workerSensor.getPace(),
			workerSensor.getStepPerMinute()
		);
	}
}