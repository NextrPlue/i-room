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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

@Service
@Transactional
@RequiredArgsConstructor
public class WorkerSensorService {

	private final KafkaProducerService kafkaProducerService;
	private final WorkerSensorRepository workerSensorRepository;
	private final WorkerReadModelRepository workerReadModelRepository;

	// 근로자 센서 업데이트
	@PreAuthorize("hasAuthority('ROLE_WORKER')")
	public WorkerSensorUpdateResponse updateSensor(Long workerId, byte[] binaryData) throws IOException {
		workerReadModelRepository.findById(workerId)
			.orElseThrow(() -> new EntityNotFoundException("유효하지 않은 근로자"));

		WorkerSensor workerSensor = workerSensorRepository.findByWorkerId(workerId)
			.orElseGet(() -> {
				WorkerSensor newSensor = WorkerSensor.builder().workerId(workerId).build();
				return workerSensorRepository.save(newSensor);
			});

		WorkerSensorUpdateRequest request = parseBinaryData(binaryData);

		workerSensor.updateSensor(request.latitude(), request.longitude(), request.heartRate(),
			request.steps(), request.speed(), request.pace(), request.stepPerMinute());

		WorkerSensorEvent workerSensorEvent = new WorkerSensorEvent(
			workerSensor.getWorkerId(),
			workerSensor.getLatitude(),
			workerSensor.getLongitude(),
			workerSensor.getHeartRate(),
			workerSensor.getSteps(),
			workerSensor.getSpeed(),
			workerSensor.getPace(),
			workerSensor.getStepPerMinute()
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

	private WorkerSensorUpdateRequest parseBinaryData(byte[] binaryData) throws IOException {
		try (ByteArrayInputStream bis = new ByteArrayInputStream(binaryData);
			 DataInputStream dis = new DataInputStream(bis)) {

			Double latitude = dis.readDouble();
			Double longitude = dis.readDouble();
			Double heartRate = dis.readDouble();
			Long steps = dis.readLong();
			Double speed = dis.readDouble();
			Double pace = dis.readDouble();
			Long stepPerMinute = dis.readLong();

			return new WorkerSensorUpdateRequest(latitude, longitude, heartRate, steps, speed, pace, stepPerMinute);
		}
	}
}
