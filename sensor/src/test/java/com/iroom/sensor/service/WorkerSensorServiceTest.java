package com.iroom.sensor.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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