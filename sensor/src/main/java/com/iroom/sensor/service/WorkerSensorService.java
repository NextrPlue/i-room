package com.iroom.sensor.service;

import com.iroom.modulecommon.dto.event.WorkerSensorEvent;
import com.iroom.modulecommon.exception.CustomException;
import com.iroom.modulecommon.exception.ErrorCode;
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

@Service
@Transactional
@RequiredArgsConstructor
public class WorkerSensorService {

	private final KafkaProducerService kafkaProducerService;
	private final WorkerSensorRepository workerSensorRepository;
	private final WorkerReadModelRepository workerReadModelRepository;

	// 근로자 센서 업데이트
	@PreAuthorize("hasAuthority('ROLE_WORKER')")
	public WorkerSensorUpdateResponse updateSensor(Long workerId, byte[] binaryData) {
		workerReadModelRepository.findById(workerId)
			.orElseThrow(() -> new CustomException(ErrorCode.SENSOR_WORKER_NOT_FOUND));

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
			.orElseThrow(() -> new CustomException(ErrorCode.SENSOR_WORKER_NOT_FOUND));

		return new WorkerLocationResponse(workerSensor);
	}

	private WorkerSensorUpdateRequest parseBinaryData(byte[] binaryData) {
		try (ByteArrayInputStream bis = new ByteArrayInputStream(binaryData);
			 DataInputStream dis = new DataInputStream(bis)) {

			Double latitude = dis.readDouble();
			Double longitude = dis.readDouble();
			Double heartRate = dis.readDouble();
			Long steps = dis.readLong();
			Double speed = dis.readDouble();
			Double pace = dis.readDouble();
			Long stepPerMinute = dis.readLong();

			validateCoordinates(latitude, longitude);
			validateBiometricData(heartRate, steps, speed, pace, stepPerMinute);

			return new WorkerSensorUpdateRequest(latitude, longitude, heartRate, steps, speed, pace, stepPerMinute);
		} catch (Exception e) {
			throw new CustomException(ErrorCode.SENSOR_INVALID_BINARY_DATA);
		}
	}

	private void validateCoordinates(Double latitude, Double longitude) {
		if (latitude == null || longitude == null ||
			latitude < -90 || latitude > 90 ||
			longitude < -180 || longitude > 180) {
			throw new CustomException(ErrorCode.SENSOR_INVALID_COORDINATES);
		}
	}

	private void validateBiometricData(Double heartRate, Long steps, Double speed, Double pace, Long stepPerMinute) {
		if (heartRate != null && (heartRate < 0 || heartRate > 300)) {
			throw new CustomException(ErrorCode.SENSOR_INVALID_BIOMETRIC_DATA);
		}
		if (steps != null && steps < 0) {
			throw new CustomException(ErrorCode.SENSOR_INVALID_BIOMETRIC_DATA);
		}
		if (speed != null && speed < 0) {
			throw new CustomException(ErrorCode.SENSOR_INVALID_BIOMETRIC_DATA);
		}
		if (pace != null && pace < 0) {
			throw new CustomException(ErrorCode.SENSOR_INVALID_BIOMETRIC_DATA);
		}
		if (stepPerMinute != null && stepPerMinute < 0) {
			throw new CustomException(ErrorCode.SENSOR_INVALID_BIOMETRIC_DATA);
		}
	}
}
