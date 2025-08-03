package com.iroom.sensor.service;

import com.iroom.modulecommon.service.KafkaProducerService;
import com.iroom.sensor.dto.WorkerHealth.WorkerUpdateLocationRequest;
import com.iroom.sensor.dto.WorkerHealth.WorkerUpdateLocationResponse;
import com.iroom.sensor.dto.WorkerHealth.WorkerUpdateVitalSignsRequest;
import com.iroom.sensor.dto.WorkerHealth.WorkerUpdateVitalSignsResponse;
import com.iroom.modulecommon.dto.event.WorkerLocationEvent;
import com.iroom.modulecommon.dto.event.WorkerVitalSignsEvent;
import com.iroom.sensor.entity.WorkerHealth;
import com.iroom.sensor.repository.WorkerHealthRepository;
import com.iroom.sensor.repository.WorkerReadModelRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class WorkerHealthService {

	private final KafkaProducerService kafkaProducerService;
	private final WorkerHealthRepository workerHealthRepository;
	private final WorkerReadModelRepository workerReadModelRepository;

	//위치 업데이트 기능
	@PreAuthorize("hasAuthority('ROLE_EQUIPMENT_SYSTEM')")
	public WorkerUpdateLocationResponse updateLocation(WorkerUpdateLocationRequest request) {
		workerReadModelRepository.findById(request.workerId())
			.orElseThrow(() -> new EntityNotFoundException("유효하지 않은 근로자"));

		WorkerHealth health = workerHealthRepository.findByWorkerId(request.workerId())
			.orElseGet(() -> {
				WorkerHealth newHealth = WorkerHealth.builder().workerId(request.workerId()).build();
				return workerHealthRepository.save(newHealth);
			});

		health.updateLocation(request.latitude(), request.longitude());

		WorkerLocationEvent workerLocationEvent = new WorkerLocationEvent(
			health.getWorkerId(),
			health.getLatitude(),
			health.getLongitude()
		);

		kafkaProducerService.publishMessage("WORKER_LOCATION_UPDATED", workerLocationEvent);

		return new WorkerUpdateLocationResponse(health.getWorkerId(), health.getLatitude(), health.getLongitude());
	}

	//생체정보 업데이트 기능
	@PreAuthorize("hasAuthority('ROLE_EQUIPMENT_SYSTEM')")
	public WorkerUpdateVitalSignsResponse updateVitalSigns(WorkerUpdateVitalSignsRequest request) {
		workerReadModelRepository.findById(request.workerId())
			.orElseThrow(() -> new EntityNotFoundException("유효하지 않은 근로자"));

		WorkerHealth health = workerHealthRepository.findByWorkerId(request.workerId())
			.orElseGet(() -> {
				WorkerHealth newHealth = WorkerHealth.builder().workerId(request.workerId()).build();
				return workerHealthRepository.save(newHealth);
			});

		health.updateVitalSign(request.heartRate(), request.bodyTemperature());

		WorkerVitalSignsEvent workerVitalSignsEvent = new WorkerVitalSignsEvent(
			health.getWorkerId(),
			health.getHeartRate(),
			health.getBodyTemperature()
		);

		kafkaProducerService.publishMessage("WORKER_VITAL_SIGNS_UPDATED", workerVitalSignsEvent);

		return new WorkerUpdateVitalSignsResponse(
			health.getWorkerId(),
			health.getHeartRate(),
			health.getBodyTemperature()
		);
	}

	//위치 조회 기능
	@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_READER')")
	public WorkerUpdateLocationResponse getWorkerLocation(Long workerId) {
		WorkerHealth health = workerHealthRepository.findByWorkerId(workerId)
			.orElseThrow(() -> new EntityNotFoundException("해당 근로자 없음"));

		return new WorkerUpdateLocationResponse(health);
	}
}
