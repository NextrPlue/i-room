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
import com.iroom.sensor.dto.WorkerSensor.*;
import com.iroom.sensor.entity.WorkerSensor;
import com.iroom.sensor.entity.WorkerReadModel;
import com.iroom.sensor.repository.WorkerSensorRepository;
import com.iroom.sensor.repository.WorkerReadModelRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
public class WorkerSensorServiceTest {

	@Mock
	private WorkerSensorRepository workerRepository;

	@Mock
	private WorkerReadModelRepository workerReadModelRepository;

	@Mock
	private KafkaProducerService kafkaProducerService;

	@InjectMocks
	private WorkerSensorService workerService;

	@Test
	@DisplayName("근로자 위치 업데이트 테스트")
	void updateLocationTest() {
		// given
		Long workerId = 1L;
		Double latitude = 35.8343;
		Double longitude = 128.4723;
		WorkerUpdateLocationRequest request = new WorkerUpdateLocationRequest(workerId, latitude, longitude);

		WorkerReadModel workerReadModel = WorkerReadModel.builder()
			.id(workerId)
			.name("테스트 근로자")
			.build();
		given(workerReadModelRepository.findById(workerId)).willReturn(Optional.of(workerReadModel));

		WorkerSensor workerSensor = WorkerSensor.builder()
			.workerId(workerId)
			.build();
		given(workerRepository.findByWorkerId(workerId)).willReturn(Optional.of(workerSensor));

		// when
		var response = workerService.updateLocation(request);

		// then
		assertThat(response.workerId()).isEqualTo(workerId);
		assertThat(response.latitude()).isEqualTo(latitude);
		assertThat(response.longitude()).isEqualTo(longitude);
		verify(kafkaProducerService).publishMessage(eq("WORKER_LOCATION_UPDATED"), any());
		verify(workerReadModelRepository).findById(workerId);
		verify(workerRepository).findByWorkerId(workerId);
	}

	@Test
	@DisplayName("없는 workerId로 위치 업데이트 시 실패 테스트")
	void updateLocationFailTest() {
		// given
		Long invalidId = 999L;
		Double latitude = 35.8343;
		Double longitude = 128.4723;
		WorkerUpdateLocationRequest request = new WorkerUpdateLocationRequest(invalidId, latitude, longitude);
		given(workerReadModelRepository.findById(invalidId)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> workerService.updateLocation(request))
			.isInstanceOf(EntityNotFoundException.class)
			.hasMessageContaining("유효하지 않은 근로자");
	}

	@Test
	@DisplayName("근로자 생체 정보 업데이트 테스트")
	void updateVitalSignsTest() {
		// given
		Long workerId = 1L;
		Integer newHeartRate = 85;
		Float newTemperature = 36.8F;
		WorkerUpdateVitalSignsRequest request = new WorkerUpdateVitalSignsRequest(workerId, newHeartRate,
			newTemperature);

		WorkerReadModel workerReadModel = WorkerReadModel.builder()
			.id(workerId)
			.name("테스트 근로자")
			.build();
		given(workerReadModelRepository.findById(workerId)).willReturn(Optional.of(workerReadModel));

		WorkerSensor workerSensor = WorkerSensor.builder().workerId(workerId).build();
		given(workerRepository.findByWorkerId(workerId)).willReturn(Optional.of(workerSensor));

		// when
		var response = workerService.updateVitalSigns(request);

		// then
		assertThat(response.workerId()).isEqualTo(workerId);
		assertThat(response.heartRate()).isEqualTo(newHeartRate);
		assertThat(response.bodyTemperature()).isEqualTo(newTemperature);
		verify(kafkaProducerService).publishMessage(eq("WORKER_VITAL_SIGNS_UPDATED"), any());
		verify(workerReadModelRepository).findById(workerId);
		verify(workerRepository).findByWorkerId(workerId);
	}

	@Test
	@DisplayName("없는 workerId로 생체 정보 업데이트 시 실패 테스트")
	void updateVitalSignsFailTest() {
		// given
		Long invalidId = 999L;
		WorkerUpdateVitalSignsRequest request = new WorkerUpdateVitalSignsRequest(invalidId, 80, 36.5F);
		given(workerReadModelRepository.findById(invalidId)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> workerService.updateVitalSigns(request))
			.isInstanceOf(EntityNotFoundException.class)
			.hasMessageContaining("유효하지 않은 근로자");
	}

	@Test
	@DisplayName("workerId로 위치 조회 테스트")
	void getWorkerLocationTest() {
		//given
		Long workerId = 1L;
		Double latitude = 35.8343;
		Double longitude = 128.4723;
		WorkerSensor workerSensor = WorkerSensor.builder().workerId(workerId).build();
		workerSensor.updateLocation(latitude, longitude);
		given(workerRepository.findByWorkerId(workerId)).willReturn(Optional.of(workerSensor));

		// when
		WorkerUpdateLocationResponse response = workerService.getWorkerLocation(workerId);

		// then
		assertThat(response.workerId()).isEqualTo(workerId);
		assertThat(response.latitude()).isEqualTo(latitude);
		assertThat(response.longitude()).isEqualTo(longitude);
		verify(workerRepository).findByWorkerId(workerId);
	}

}
