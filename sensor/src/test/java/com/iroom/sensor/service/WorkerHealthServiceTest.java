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

import com.iroom.sensor.dto.WorkerHealth.WorkerUpdateLocationRequest;
import com.iroom.sensor.dto.WorkerHealth.WorkerUpdateLocationResponse;
import com.iroom.sensor.dto.WorkerHealth.WorkerUpdateVitalSignsRequest;
import com.iroom.sensor.entity.WorkerHealth;
import com.iroom.sensor.repository.WorkerHealthRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
public class WorkerHealthServiceTest {

	@Mock
	private WorkerHealthRepository workerRepository;

	@InjectMocks
	private WorkerHealthService workerService;

	@Test
	@DisplayName("근로자 위치 업데이트 테스트")
	void updateLocationTest() {
		Long workerId = 1L;
		String newLocation = "35.8343, 128.4723";
		WorkerUpdateLocationRequest request = new WorkerUpdateLocationRequest(workerId, newLocation);

		WorkerHealth workerHealth = WorkerHealth.builder()
			.workerId(workerId)
			.build();

		given(workerRepository.findByWorkerId(workerId)).willReturn(Optional.of(workerHealth));

		var response = workerService.updateLocation(request);

		assertThat(response.workerId()).isEqualTo(workerId);
		assertThat(response.location()).isEqualTo(newLocation);
		verify(workerRepository).findByWorkerId(workerId);
	}

	@Test
	@DisplayName("없는 workerId로 위치 업데이트 시 실패 테스트")
	void updateLocationFailTest() {
		Long invalidId = 999L;
		WorkerUpdateLocationRequest request = new WorkerUpdateLocationRequest(invalidId, "354.8343, 128.4723");
		given(workerRepository.findByWorkerId(invalidId)).willReturn(Optional.empty());

		assertThatThrownBy(() -> workerService.updateLocation(request))
			.isInstanceOf(EntityNotFoundException.class)
			.hasMessageContaining("해당 근로자 없음");
	}

	@Test
	@DisplayName("근로자 생체 정보 업데이트 테스트")
	void updateVitalSignsTest() {
		Long workerId = 1L;
		Integer newHeartRate = 85;
		Float newTemperature = 36.8F;
		WorkerUpdateVitalSignsRequest request = new WorkerUpdateVitalSignsRequest(workerId, newHeartRate,
			newTemperature);

		WorkerHealth workerHealth = WorkerHealth.builder().workerId(workerId).build();
		given(workerRepository.findByWorkerId(workerId)).willReturn(Optional.of(workerHealth));

		var response = workerService.updateVitalSigns(request);

		assertThat(response.workerId()).isEqualTo(workerId);
		assertThat(response.heartRate()).isEqualTo(newHeartRate);
		assertThat(response.bodyTemperature()).isEqualTo(newTemperature);
		verify(workerRepository).findByWorkerId(workerId);
	}

	@Test
	@DisplayName("없는 workerId로 생체 정보 업데이트 시 실패 테스트")
	void updateVitalSignsFailTest() {
		Long invalidId = 999L;
		WorkerUpdateVitalSignsRequest request = new WorkerUpdateVitalSignsRequest(invalidId, 80, 36.5F);
		given(workerRepository.findByWorkerId(invalidId)).willReturn(Optional.empty());

		assertThatThrownBy(() -> workerService.updateVitalSigns(request))
			.isInstanceOf(EntityNotFoundException.class)
			.hasMessageContaining("해당 근로자 없음");
	}

	@Test
	@DisplayName("workerId로 위치 조회 테스트")
	void getWorkerLocationTest() {
		Long workerId = 1L;
		String location = "51.5072, 0.1275";

		WorkerHealth workerHealth = WorkerHealth.builder().workerId(workerId).build();

		workerHealth.updateLocation(location);

		given(workerRepository.findByWorkerId(workerId)).willReturn(Optional.of(workerHealth));

		WorkerUpdateLocationResponse response = workerService.getWorkerLocation(workerId);

		assertThat(response.workerId()).isEqualTo(workerId);
		assertThat(response.location()).isEqualTo(location);
		verify(workerRepository).findByWorkerId(workerId);
	}

}
