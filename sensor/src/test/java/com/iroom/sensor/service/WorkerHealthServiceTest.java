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
import com.iroom.sensor.dto.WorkerHealth.*;
import com.iroom.sensor.entity.WorkerHealth;
import com.iroom.sensor.repository.WorkerHealthRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
public class WorkerHealthServiceTest {

	@Mock
	private WorkerHealthRepository workerRepository;

	@Mock
	private KafkaProducerService kafkaProducerService;

	@InjectMocks
	private WorkerHealthService workerService;

	@Test
	@DisplayName("근로자 위치 업데이트 테스트")
	void updateLocationTest() {
		// given
		Long workerId = 1L;
		Double latitude = 35.8343;
		Double longitude = 128.4723;
		WorkerUpdateLocationRequest request = new WorkerUpdateLocationRequest(workerId, latitude, longitude);

		WorkerHealth workerHealth = WorkerHealth.builder()
			.workerId(workerId)
			.build();
		given(workerRepository.findByWorkerId(workerId)).willReturn(Optional.of(workerHealth));

		// when
		var response = workerService.updateLocation(request);

		// then
		assertThat(response.workerId()).isEqualTo(workerId);
		assertThat(response.latitude()).isEqualTo(latitude);
		assertThat(response.longitude()).isEqualTo(longitude);
		verify(kafkaProducerService).publishMessage(eq("Worker_Location"), any());
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
		given(workerRepository.findByWorkerId(invalidId)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> workerService.updateLocation(request))
			.isInstanceOf(EntityNotFoundException.class)
			.hasMessageContaining("해당 근로자 없음");
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
		WorkerHealth workerHealth = WorkerHealth.builder().workerId(workerId).build();
		given(workerRepository.findByWorkerId(workerId)).willReturn(Optional.of(workerHealth));

		// when
		var response = workerService.updateVitalSigns(request);

		// then
		assertThat(response.workerId()).isEqualTo(workerId);
		assertThat(response.heartRate()).isEqualTo(newHeartRate);
		assertThat(response.bodyTemperature()).isEqualTo(newTemperature);
		verify(workerRepository).findByWorkerId(workerId);
	}

	@Test
	@DisplayName("없는 workerId로 생체 정보 업데이트 시 실패 테스트")
	void updateVitalSignsFailTest() {
		// given
		Long invalidId = 999L;
		WorkerUpdateVitalSignsRequest request = new WorkerUpdateVitalSignsRequest(invalidId, 80, 36.5F);
		given(workerRepository.findByWorkerId(invalidId)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> workerService.updateVitalSigns(request))
			.isInstanceOf(EntityNotFoundException.class)
			.hasMessageContaining("해당 근로자 없음");
	}

	@Test
	@DisplayName("workerId로 위치 조회 테스트")
	void getWorkerLocationTest() {
		//given
		Long workerId = 1L;
		Double latitude = 35.8343;
		Double longitude = 128.4723;
		WorkerHealth workerHealth = WorkerHealth.builder().workerId(workerId).build();
		workerHealth.updateLocation(latitude, longitude);
		given(workerRepository.findByWorkerId(workerId)).willReturn(Optional.of(workerHealth));

		// when
		WorkerUpdateLocationResponse response = workerService.getWorkerLocation(workerId);

		// then
		assertThat(response.workerId()).isEqualTo(workerId);
		assertThat(response.latitude()).isEqualTo(latitude);
		assertThat(response.longitude()).isEqualTo(longitude);
		verify(workerRepository).findByWorkerId(workerId);
	}

}
