package com.iroom.sensor.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.iroom.modulecommon.service.KafkaProducerService;
import com.iroom.sensor.dto.WorkerSensor.WorkerSensorUpdateRequest;
import com.iroom.sensor.dto.WorkerSensor.WorkerSensorUpdateResponse;
import com.iroom.sensor.dto.WorkerSensor.WorkerLocationResponse;
import com.iroom.sensor.entity.WorkerSensor;
import com.iroom.sensor.entity.WorkerReadModel;
import com.iroom.sensor.repository.WorkerSensorRepository;
import com.iroom.sensor.repository.WorkerReadModelRepository;

import com.iroom.modulecommon.exception.CustomException;
import com.iroom.modulecommon.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
public class WorkerSensorServiceTest {

	@Mock
	private WorkerSensorRepository workerSensorRepository;

	@Mock
	private WorkerReadModelRepository workerReadModelRepository;

	@Mock
	private KafkaProducerService kafkaProducerService;

	@Mock
	private SimpMessagingTemplate messagingTemplate;

	@InjectMocks
	private WorkerSensorService workerSensorService;

	@Test
	@DisplayName("통합 센서 데이터 업데이트 테스트 - 모든 데이터")
	void updateSensorAllDataTest() throws IOException {
		// given
		Long workerId = 1L;
		Double latitude = 35.8343;
		Double longitude = 128.4723;
		Double heartRate = 85.0;
		Long steps = 1000L;
		Double speed = 5.5;
		Double pace = 10.9;
		Long stepPerMinute = 120L;
		
		byte[] binaryData = createBinaryData(latitude, longitude, heartRate, steps, speed, pace, stepPerMinute);

		WorkerReadModel workerReadModel = WorkerReadModel.builder()
			.id(workerId)
			.name("테스트 근로자")
			.build();

		WorkerSensor workerSensor = WorkerSensor.builder()
			.workerId(workerId)
			.build();

		given(workerReadModelRepository.findById(workerId)).willReturn(Optional.of(workerReadModel));
		given(workerSensorRepository.findByWorkerId(workerId)).willReturn(Optional.of(workerSensor));

		// when
		WorkerSensorUpdateResponse response = workerSensorService.updateSensor(workerId, binaryData);

		// then
		assertThat(response.workerId()).isEqualTo(workerId);
		assertThat(response.latitude()).isEqualTo(latitude);
		assertThat(response.longitude()).isEqualTo(longitude);
		assertThat(response.heartRate()).isEqualTo(heartRate);
		assertThat(response.steps()).isEqualTo(steps);
		assertThat(response.speed()).isEqualTo(speed);
		assertThat(response.pace()).isEqualTo(pace);
		assertThat(response.stepPerMinute()).isEqualTo(stepPerMinute);
		
		verify(kafkaProducerService).publishMessage(eq("WORKER_SENSOR_UPDATED"), any());
		verify(messagingTemplate).convertAndSend(eq("/sensor/topic/sensors/admin"), anyString());
	}

	@Test
	@DisplayName("통합 센서 데이터 업데이트 테스트 - 위치만")
	void updateSensorLocationOnlyTest() throws IOException {
		// given
		Long workerId = 2L;
		Double latitude = 35.8343;
		Double longitude = 128.4723;
		Double heartRate = 0.0;
		Long steps = 0L;
		Double speed = 0.0;
		Double pace = 0.0;
		Long stepPerMinute = 0L;
		
		byte[] binaryData = createBinaryData(latitude, longitude, heartRate, steps, speed, pace, stepPerMinute);

		WorkerReadModel workerReadModel = WorkerReadModel.builder()
			.id(workerId)
			.name("테스트 근로자")
			.build();

		WorkerSensor workerSensor = WorkerSensor.builder()
			.workerId(workerId)
			.build();

		given(workerReadModelRepository.findById(workerId)).willReturn(Optional.of(workerReadModel));
		given(workerSensorRepository.findByWorkerId(workerId)).willReturn(Optional.of(workerSensor));

		// when
		WorkerSensorUpdateResponse response = workerSensorService.updateSensor(workerId, binaryData);

		// then
		assertThat(response.workerId()).isEqualTo(workerId);
		assertThat(response.latitude()).isEqualTo(latitude);
		assertThat(response.longitude()).isEqualTo(longitude);
		assertThat(response.heartRate()).isEqualTo(heartRate);
		
		verify(kafkaProducerService).publishMessage(eq("WORKER_SENSOR_UPDATED"), any());
		verify(messagingTemplate).convertAndSend(eq("/sensor/topic/sensors/admin"), anyString());
	}

	@Test
	@DisplayName("통합 센서 데이터 업데이트 테스트 - 심박수만")
	void updateSensorHeartRateOnlyTest() throws IOException {
		// given
		Long workerId = 3L;
		Double latitude = 0.0;
		Double longitude = 0.0;
		Double heartRate = 90.0;
		Long steps = 0L;
		Double speed = 0.0;
		Double pace = 0.0;
		Long stepPerMinute = 0L;
		
		byte[] binaryData = createBinaryData(latitude, longitude, heartRate, steps, speed, pace, stepPerMinute);

		WorkerReadModel workerReadModel = WorkerReadModel.builder()
			.id(workerId)
			.name("테스트 근로자")
			.build();

		WorkerSensor workerSensor = WorkerSensor.builder()
			.workerId(workerId)
			.build();

		given(workerReadModelRepository.findById(workerId)).willReturn(Optional.of(workerReadModel));
		given(workerSensorRepository.findByWorkerId(workerId)).willReturn(Optional.of(workerSensor));

		// when
		WorkerSensorUpdateResponse response = workerSensorService.updateSensor(workerId, binaryData);

		// then
		assertThat(response.workerId()).isEqualTo(workerId);
		assertThat(response.latitude()).isEqualTo(latitude);
		assertThat(response.longitude()).isEqualTo(longitude);
		assertThat(response.heartRate()).isEqualTo(heartRate);
		
		verify(kafkaProducerService).publishMessage(eq("WORKER_SENSOR_UPDATED"), any());
		verify(messagingTemplate).convertAndSend(eq("/sensor/topic/sensors/admin"), anyString());
	}

	@Test
	@DisplayName("새 근로자 센서 생성 테스트")
	void createNewWorkerSensorTest() throws IOException {
		// given
		Long workerId = 4L;
		Double latitude = 35.8343;
		Double longitude = 128.4723;
		Double heartRate = 75.0;
		Long steps = 500L;
		Double speed = 3.2;
		Double pace = 18.75;
		Long stepPerMinute = 80L;
		
		byte[] binaryData = createBinaryData(latitude, longitude, heartRate, steps, speed, pace, stepPerMinute);

		WorkerReadModel workerReadModel = WorkerReadModel.builder()
			.id(workerId)
			.name("신규 근로자")
			.build();

		WorkerSensor newWorkerSensor = WorkerSensor.builder()
			.workerId(workerId)
			.build();

		given(workerReadModelRepository.findById(workerId)).willReturn(Optional.of(workerReadModel));
		given(workerSensorRepository.findByWorkerId(workerId)).willReturn(Optional.empty());
		given(workerSensorRepository.save(any(WorkerSensor.class))).willReturn(newWorkerSensor);

		// when
		WorkerSensorUpdateResponse response = workerSensorService.updateSensor(workerId, binaryData);

		// then
		assertThat(response.workerId()).isEqualTo(workerId);
		verify(workerSensorRepository).save(any(WorkerSensor.class));
		verify(kafkaProducerService).publishMessage(eq("WORKER_SENSOR_UPDATED"), any());
		verify(messagingTemplate).convertAndSend(eq("/sensor/topic/sensors/admin"), anyString());
	}

	@Test
	@DisplayName("위치 정보 조회 테스트")
	void getWorkerLocationTest() {
		// given
		Long workerId = 5L;
		Double latitude = 35.8343;
		Double longitude = 128.4723;
		Double heartRate = 80.0;
		Long steps = 200L;
		Double speed = 2.1;
		Double pace = 28.6;
		Long stepPerMinute = 60L;

		WorkerSensor workerSensor = WorkerSensor.builder()
			.workerId(workerId)
			.build();
		workerSensor.updateSensor(latitude, longitude, heartRate, steps, speed, pace, stepPerMinute);

		given(workerSensorRepository.findByWorkerId(workerId)).willReturn(Optional.of(workerSensor));

		// when
		WorkerLocationResponse response = workerSensorService.getWorkerLocation(workerId);

		// then
		assertThat(response.workerId()).isEqualTo(workerId);
		assertThat(response.latitude()).isEqualTo(latitude);
		assertThat(response.longitude()).isEqualTo(longitude);
	}

	@Test
	@DisplayName("존재하지 않는 근로자 업데이트 시 예외 발생 테스트")
	void updateNonExistentWorkerTest() throws IOException {
		// given
		Long workerId = 999L;
		byte[] binaryData = createBinaryData(35.0, 128.0, 80.0, 100L, 1.5, 40.0, 50L);

		given(workerReadModelRepository.findById(workerId)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> workerSensorService.updateSensor(workerId, binaryData))
			.isInstanceOf(CustomException.class)
			.extracting(e -> ((CustomException) e).getErrorCode())
			.isEqualTo(ErrorCode.SENSOR_WORKER_NOT_FOUND);
	}

	@Test
	@DisplayName("존재하지 않는 근로자 위치 조회 시 예외 발생 테스트")
	void getNonExistentWorkerLocationTest() {
		// given
		Long workerId = 999L;

		given(workerSensorRepository.findByWorkerId(workerId)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> workerSensorService.getWorkerLocation(workerId))
			.isInstanceOf(CustomException.class)
			.extracting(e -> ((CustomException) e).getErrorCode())
			.isEqualTo(ErrorCode.SENSOR_WORKER_NOT_FOUND);
	}

	@Test
	@DisplayName("다중 근로자 위치 정보 조회 성공")
	void getWorkersLocationTest() {
		// given
		List<Long> workerIds = Arrays.asList(1L, 2L, 3L);
		
		WorkerSensor sensor1 = WorkerSensor.builder()
			.workerId(1L)
			.build();
		sensor1.updateSensor(37.5665, 126.9780, 80.0, 1000L, 5.5, 10.9, 120L);
		
		WorkerSensor sensor2 = WorkerSensor.builder()
			.workerId(2L)
			.build();
		sensor2.updateSensor(37.5666, 126.9781, 75.0, 800L, 4.2, 14.3, 100L);
		
		WorkerSensor sensor3 = WorkerSensor.builder()
			.workerId(3L)
			.build();
		sensor3.updateSensor(37.5667, 126.9782, 90.0, 1200L, 6.1, 9.8, 140L);
		
		List<WorkerSensor> sensors = Arrays.asList(sensor1, sensor2, sensor3);
		
		given(workerSensorRepository.findByWorkerIdIn(workerIds)).willReturn(sensors);

		// when
		List<WorkerLocationResponse> response = workerSensorService.getWorkersLocation(workerIds);

		// then
		assertThat(response).hasSize(3);
		
		assertThat(response.get(0).workerId()).isEqualTo(1L);
		assertThat(response.get(0).latitude()).isEqualTo(37.5665);
		assertThat(response.get(0).longitude()).isEqualTo(126.9780);
		
		assertThat(response.get(1).workerId()).isEqualTo(2L);
		assertThat(response.get(1).latitude()).isEqualTo(37.5666);
		assertThat(response.get(1).longitude()).isEqualTo(126.9781);
		
		assertThat(response.get(2).workerId()).isEqualTo(3L);
		assertThat(response.get(2).latitude()).isEqualTo(37.5667);
		assertThat(response.get(2).longitude()).isEqualTo(126.9782);
	}

	@Test
	@DisplayName("다중 근로자 위치 정보 조회 - 빈 리스트")
	void getWorkersLocation_emptyListTest() {
		// given
		List<Long> emptyWorkerIds = Collections.emptyList();

		// when
		List<WorkerLocationResponse> response = workerSensorService.getWorkersLocation(emptyWorkerIds);

		// then
		assertThat(response).isEmpty();
		verify(workerSensorRepository, never()).findByWorkerIdIn(any());
	}

	@Test
	@DisplayName("다중 근로자 위치 정보 조회 - null 리스트")
	void getWorkersLocation_nullListTest() {
		// when
		List<WorkerLocationResponse> response = workerSensorService.getWorkersLocation(null);

		// then
		assertThat(response).isEmpty();
		verify(workerSensorRepository, never()).findByWorkerIdIn(any());
	}

	@Test
	@DisplayName("다중 근로자 위치 정보 조회 - 일부 근로자만 센서 데이터 존재")
	void getWorkersLocation_partialDataTest() {
		// given
		List<Long> workerIds = Arrays.asList(1L, 2L, 3L);
		
		WorkerSensor sensor1 = WorkerSensor.builder()
			.workerId(1L)
			.build();
		sensor1.updateSensor(37.5665, 126.9780, 80.0, 1000L, 5.5, 10.9, 120L);
		
		// workerId 2L, 3L은 센서 데이터가 없음 (DB에 데이터가 없는 상황)
		List<WorkerSensor> sensors = Arrays.asList(sensor1); // 1개만 있음
		
		given(workerSensorRepository.findByWorkerIdIn(workerIds)).willReturn(sensors);

		// when
		List<WorkerLocationResponse> response = workerSensorService.getWorkersLocation(workerIds);

		// then
		assertThat(response).hasSize(1); // 센서 데이터가 있는 1명만 반환
		assertThat(response.get(0).workerId()).isEqualTo(1L);
		assertThat(response.get(0).latitude()).isEqualTo(37.5665);
		assertThat(response.get(0).longitude()).isEqualTo(126.9780);
	}

	private byte[] createBinaryData(Double latitude, Double longitude, Double heartRate, 
									Long steps, Double speed, Double pace, Long stepPerMinute) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		
		dos.writeDouble(latitude);
		dos.writeDouble(longitude);
		dos.writeDouble(heartRate);
		dos.writeLong(steps);
		dos.writeDouble(speed);
		dos.writeDouble(pace);
		dos.writeLong(stepPerMinute);
		
		return bos.toByteArray();
	}
}