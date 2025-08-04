package com.iroom.sensor.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

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

import jakarta.persistence.EntityNotFoundException;

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
	void updateSensorAllDataTest() {
		// given
		Long workerId = 1L;
		Double latitude = 35.8343;
		Double longitude = 128.4723;
		Integer heartRate = 85;
		
		WorkerSensorUpdateRequest request = new WorkerSensorUpdateRequest(workerId, latitude, longitude, heartRate);

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
		WorkerSensorUpdateResponse response = workerSensorService.updateSensor(request);

		// then
		assertThat(response.workerId()).isEqualTo(workerId);
		assertThat(response.latitude()).isEqualTo(latitude);
		assertThat(response.longitude()).isEqualTo(longitude);
		assertThat(response.heartRate()).isEqualTo(heartRate);
		
		verify(kafkaProducerService).publishMessage(eq("WORKER_SENSOR_UPDATED"), any());
	}

	@Test
	@DisplayName("통합 센서 데이터 업데이트 테스트 - 위치만")
	void updateSensorLocationOnlyTest() {
		// given
		Long workerId = 2L;
		Double latitude = 35.8343;
		Double longitude = 128.4723;
		Integer heartRate = null;
		
		WorkerSensorUpdateRequest request = new WorkerSensorUpdateRequest(workerId, latitude, longitude, heartRate);

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
		WorkerSensorUpdateResponse response = workerSensorService.updateSensor(request);

		// then
		assertThat(response.workerId()).isEqualTo(workerId);
		assertThat(response.latitude()).isEqualTo(latitude);
		assertThat(response.longitude()).isEqualTo(longitude);
		assertThat(response.heartRate()).isNull();
		
		verify(kafkaProducerService).publishMessage(eq("WORKER_SENSOR_UPDATED"), any());
	}

	@Test
	@DisplayName("통합 센서 데이터 업데이트 테스트 - 심박수만")
	void updateSensorHeartRateOnlyTest() {
		// given
		Long workerId = 3L;
		Double latitude = null;
		Double longitude = null;
		Integer heartRate = 90;
		
		WorkerSensorUpdateRequest request = new WorkerSensorUpdateRequest(workerId, latitude, longitude, heartRate);

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
		WorkerSensorUpdateResponse response = workerSensorService.updateSensor(request);

		// then
		assertThat(response.workerId()).isEqualTo(workerId);
		assertThat(response.latitude()).isNull();
		assertThat(response.longitude()).isNull();
		assertThat(response.heartRate()).isEqualTo(heartRate);
		
		verify(kafkaProducerService).publishMessage(eq("WORKER_SENSOR_UPDATED"), any());
	}

	@Test
	@DisplayName("새 근로자 센서 생성 테스트")
	void createNewWorkerSensorTest() {
		// given
		Long workerId = 4L;
		Double latitude = 35.8343;
		Double longitude = 128.4723;
		Integer heartRate = 75;
		
		WorkerSensorUpdateRequest request = new WorkerSensorUpdateRequest(workerId, latitude, longitude, heartRate);

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
		WorkerSensorUpdateResponse response = workerSensorService.updateSensor(request);

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
		Integer heartRate = 80;

		WorkerSensor workerSensor = WorkerSensor.builder()
			.workerId(workerId)
			.build();
		workerSensor.updateSensor(latitude, longitude, heartRate);

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
	void updateNonExistentWorkerTest() {
		// given
		Long workerId = 999L;
		WorkerSensorUpdateRequest request = new WorkerSensorUpdateRequest(workerId, 35.0, 128.0, 80);

		given(workerReadModelRepository.findById(workerId)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> workerSensorService.updateSensor(request))
			.isInstanceOf(EntityNotFoundException.class)
			.hasMessage("유효하지 않은 근로자");
	}

	@Test
	@DisplayName("존재하지 않는 근로자 위치 조회 시 예외 발생 테스트")
	void getNonExistentWorkerLocationTest() {
		// given
		Long workerId = 999L;

		given(workerSensorRepository.findByWorkerId(workerId)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> workerSensorService.getWorkerLocation(workerId))
			.isInstanceOf(EntityNotFoundException.class)
			.hasMessage("해당 근로자 없음");
	}
}