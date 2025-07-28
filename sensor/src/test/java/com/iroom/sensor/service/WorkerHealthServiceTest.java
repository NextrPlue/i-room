package com.iroom.sensor.service;

import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.iroom.sensor.dto.WorkerHealth.WorkerUpdateLocationRequest;
import com.iroom.sensor.entity.WorkerHealth;
import com.iroom.sensor.repository.WorkerHealthRepository;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class WorkerHealthServiceTest {

	@Mock
	private WorkerHealthRepository workerRepository;

	@InjectMocks
	private WorkerHealthService workerService;

	@Test
	@DisplayName("위치 업데이트 테스트")
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
}
