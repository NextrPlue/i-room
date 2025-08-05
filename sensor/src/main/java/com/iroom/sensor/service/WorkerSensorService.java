package com.iroom.sensor.service;

import com.iroom.modulecommon.dto.event.WorkerSensorEvent;
import com.iroom.modulecommon.service.KafkaProducerService;
import com.iroom.sensor.dto.WorkerSensor.WorkerLocationResponse;
import com.iroom.sensor.dto.WorkerSensor.WorkerSensorUpdateRequest;
import com.iroom.sensor.dto.WorkerSensor.WorkerSensorUpdateResponse;
import com.iroom.sensor.entity.WorkerSensor;
import com.iroom.sensor.repository.WorkerSensorRepository;
import com.iroom.sensor.repository.WorkerReadModelRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class WorkerSensorService {

	private final KafkaProducerService kafkaProducerService;
	private final WorkerSensorRepository workerSensorRepository;
	private final WorkerReadModelRepository workerReadModelRepository;

	// 근로자 센서 업데이트
	@PreAuthorize("hasAuthority('ROLE_WORKER')")
	public WorkerSensorUpdateResponse updateSensor(Long workerId, WorkerSensorUpdateRequest request) {
		workerReadModelRepository.findById(workerId)
			.orElseThrow(() -> new EntityNotFoundException("유효하지 않은 근로자"));

		WorkerSensor workerSensor = workerSensorRepository.findByWorkerId(workerId)
			.orElseGet(() -> {
				WorkerSensor newSensor = WorkerSensor.builder().workerId(workerId).build();
				return workerSensorRepository.save(newSensor);
			});

		workerSensor.updateSensor(request.latitude(), request.longitude(), request.heartRate());

		WorkerSensorEvent workerSensorEvent = new WorkerSensorEvent(
			workerSensor.getWorkerId(),
			workerSensor.getLatitude(),
			workerSensor.getLongitude(),
			workerSensor.getHeartRate()
		);

		kafkaProducerService.publishMessage("WORKER_SENSOR_UPDATED", workerSensorEvent);

		return new WorkerSensorUpdateResponse(workerSensor);
	}

	//위치 조회 기능
	@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_READER')")
	public WorkerLocationResponse getWorkerLocation(Long workerId) {
		WorkerSensor workerSensor = workerSensorRepository.findByWorkerId(workerId)
			.orElseThrow(() -> new EntityNotFoundException("해당 근로자 없음"));

		return new WorkerLocationResponse(workerSensor);
	}
}
