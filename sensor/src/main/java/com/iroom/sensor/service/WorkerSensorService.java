package com.iroom.sensor.service;

import com.iroom.modulecommon.service.KafkaProducerService;
import com.iroom.sensor.dto.WorkerSensor.WorkerUpdateLocationRequest;
import com.iroom.sensor.dto.WorkerSensor.WorkerUpdateLocationResponse;
import com.iroom.sensor.dto.WorkerSensor.WorkerUpdateVitalSignsRequest;
import com.iroom.sensor.dto.WorkerSensor.WorkerUpdateVitalSignsResponse;
import com.iroom.modulecommon.dto.event.WorkerLocationEvent;
import com.iroom.modulecommon.dto.event.WorkerVitalSignsEvent;
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

	//위치 업데이트 기능
	@PreAuthorize("hasAuthority('ROLE_EQUIPMENT_SYSTEM')")
	public WorkerUpdateLocationResponse updateLocation(WorkerUpdateLocationRequest request) {
		workerReadModelRepository.findById(request.workerId())
			.orElseThrow(() -> new EntityNotFoundException("유효하지 않은 근로자"));

		WorkerSensor workerSensor = workerSensorRepository.findByWorkerId(request.workerId())
			.orElseGet(() -> {
				WorkerSensor newSensor = WorkerSensor.builder().workerId(request.workerId()).build();
				return workerSensorRepository.save(newSensor);
			});

		workerSensor.updateLocation(request.latitude(), request.longitude());

		WorkerLocationEvent workerLocationEvent = new WorkerLocationEvent(
			workerSensor.getWorkerId(),
			workerSensor.getLatitude(),
			workerSensor.getLongitude()
		);

		kafkaProducerService.publishMessage("WORKER_LOCATION_UPDATED", workerLocationEvent);

		return new WorkerUpdateLocationResponse(workerSensor.getWorkerId(), workerSensor.getLatitude(),
			workerSensor.getLongitude());
	}

	//생체정보 업데이트 기능
	@PreAuthorize("hasAuthority('ROLE_EQUIPMENT_SYSTEM')")
	public WorkerUpdateVitalSignsResponse updateVitalSigns(WorkerUpdateVitalSignsRequest request) {
		workerReadModelRepository.findById(request.workerId())
			.orElseThrow(() -> new EntityNotFoundException("유효하지 않은 근로자"));

		WorkerSensor workerSensor = workerSensorRepository.findByWorkerId(request.workerId())
			.orElseGet(() -> {
				WorkerSensor newSensor = WorkerSensor.builder().workerId(request.workerId()).build();
				return workerSensorRepository.save(newSensor);
			});

		workerSensor.updateVitalSign(request.heartRate(), request.bodyTemperature());

		WorkerVitalSignsEvent workerVitalSignsEvent = new WorkerVitalSignsEvent(
			workerSensor.getWorkerId(),
			workerSensor.getHeartRate(),
			workerSensor.getBodyTemperature()
		);

		kafkaProducerService.publishMessage("WORKER_VITAL_SIGNS_UPDATED", workerVitalSignsEvent);

		return new WorkerUpdateVitalSignsResponse(
			workerSensor.getWorkerId(),
			workerSensor.getHeartRate(),
			workerSensor.getBodyTemperature()
		);
	}

	//위치 조회 기능
	@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_READER')")
	public WorkerUpdateLocationResponse getWorkerLocation(Long workerId) {
		WorkerSensor workerSensor = workerSensorRepository.findByWorkerId(workerId)
			.orElseThrow(() -> new EntityNotFoundException("해당 근로자 없음"));

		return new WorkerUpdateLocationResponse(workerSensor);
	}
}
