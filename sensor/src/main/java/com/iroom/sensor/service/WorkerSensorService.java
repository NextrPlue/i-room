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
import com.iroom.sensor.util.GpsFilter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class WorkerSensorService {

	private final KafkaProducerService kafkaProducerService;
	private final WorkerSensorRepository workerSensorRepository;
	private final WorkerReadModelRepository workerReadModelRepository;
	private final SimpMessagingTemplate messagingTemplate;
	private final GpsFilter gpsFilter = new GpsFilter();

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
		double[] filtered = gpsFilter.filter(request.latitude(), request.longitude(), request.speed());
		System.out.println("원본 위도 경도: " + request.latitude() + ", " + request.longitude());
		System.out.println("수정 위도 경도: " + filtered[0] + ", " + filtered[1]);
		workerSensor.updateSensor(filtered[0], filtered[1], request.heartRate(),
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

		// WebSocket으로 실시간 센서 데이터 전송
		sendSensorDataViaWebSocket(workerSensor);

		sendGpsToPythonServer(request.latitude(), request.longitude(), workerId);

		return new WorkerSensorUpdateResponse(workerSensor);
	}

	//위치 조회 기능
	@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_READER')")
	public WorkerLocationResponse getWorkerLocation(Long workerId) {
		WorkerSensor workerSensor = workerSensorRepository.findByWorkerId(workerId)
			.orElseThrow(() -> new CustomException(ErrorCode.SENSOR_WORKER_NOT_FOUND));

		return new WorkerLocationResponse(workerSensor);
	}

	//다중 근로자 위치 조회 기능
	@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_READER')")
	public List<WorkerLocationResponse> getWorkersLocation(List<Long> workerIds) {
		if (workerIds == null || workerIds.isEmpty()) {
			return List.of();
		}

		List<WorkerSensor> sensors = workerSensorRepository.findByWorkerIdIn(workerIds);
		return sensors.stream()
			.map(WorkerLocationResponse::new)
			.collect(Collectors.toList());
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

	private void sendGpsToPythonServer(Double latitude, Double longitude, Long workerId) {
		RestTemplate restTemplate = new RestTemplate();

		Map<String, Object> gpsData = new HashMap<>();
		gpsData.put("workerId", workerId);
		gpsData.put("latitude", latitude);
		gpsData.put("longitude", longitude);

		try {
			restTemplate.postForEntity("http://localhost:8000/gps/", gpsData, String.class);
			log.info("Python 서버에 GPS 전송 완료: {}", gpsData);
		} catch (Exception e) {
			log.error("Python 서버 GPS 전송 실패", e);
		}
	}

	private void sendSensorDataViaWebSocket(WorkerSensor workerSensor) {
		String adminMessage = String.format(
			"[센서 업데이트] 작업자 ID: %d, 위치: (%.6f, %.6f), 심박수: %.1f, 걸음수: %d",
			workerSensor.getWorkerId(),
			workerSensor.getLatitude(),
			workerSensor.getLongitude(),
			workerSensor.getHeartRate(),
			workerSensor.getSteps()
		);

		// 관리자에게 센서 데이터 전송
		messagingTemplate.convertAndSend("/sensor/topic/sensors/admin", adminMessage);
	}
}
