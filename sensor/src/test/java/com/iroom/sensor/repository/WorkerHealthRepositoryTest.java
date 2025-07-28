package com.iroom.sensor.repository;

import com.iroom.sensor.entity.WorkerHealth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class WorkerHealthRepositoryTest {
	@Autowired
	private WorkerHealthRepository workerHealthRepository;

	@Test
	@DisplayName("근로자 생체정보 저장 후 ID로 조회 테스트")
	void saveAndFindById() {
		WorkerHealth health = WorkerHealth.builder()
			.workerId(1L)
			.build();
		health.updateLocation("35.8343, 128.4723");
		health.updateVitalSign(75, 36.5f);

		WorkerHealth saved = workerHealthRepository.save(health);
		Optional<WorkerHealth> found = workerHealthRepository.findById(saved.getId());

		assertThat(found).isPresent();
		assertThat(found.get().getWorkerId()).isEqualTo(1L);
		assertThat(found.get().getWorkerLocation()).isEqualTo("35.8343, 128.4723");
		assertThat(found.get().getHeartRate()).isEqualTo(75);
		assertThat(found.get().getBodyTemperature()).isEqualTo(36.5f);
	}

	@Test
	@DisplayName("findByWorkerId 메서드 조회 테스트")
	void findByWorkerIdTest() {
		WorkerHealth health = WorkerHealth.builder()
			.workerId(2L)
			.build();
		health.updateLocation("23.8343, 78.4723");
		health.updateVitalSign(80, 36.8f);

		workerHealthRepository.save(health);

		Optional<WorkerHealth> result = workerHealthRepository.findByWorkerId(2L);
		assertThat(result).isPresent();
		assertThat(result.get().getWorkerLocation()).isEqualTo("23.8343, 78.4723");
		assertThat(result.get().getHeartRate()).isEqualTo(80);
		assertThat(result.get().getBodyTemperature()).isEqualTo(36.8f);
	}

	@Test
	@DisplayName("updateLocation 메서드 위치 수정 테스트")
	void updateLocationTest() {
		WorkerHealth health = WorkerHealth.builder()
			.workerId(3L)
			.build();
		health.updateLocation("23.8343, 78.4723");

		WorkerHealth saved = workerHealthRepository.save(health);

		saved.updateLocation("65.8343, 34.5423");
		WorkerHealth updated = workerHealthRepository.save(saved);

		Optional<WorkerHealth> result = workerHealthRepository.findById(updated.getId());
		assertThat(result).isPresent();
		assertThat(result.get().getWorkerLocation()).isEqualTo("65.8343, 34.5423");
	}

	@Test
	@DisplayName("updateVitalSign 메서드 생체정보 수정 테스트")
	void updateVitalSignTest() {
		WorkerHealth health = WorkerHealth.builder()
			.workerId(4L)
			.build();
		health.updateVitalSign(70, 36.0f);

		WorkerHealth saved = workerHealthRepository.save(health);

		saved.updateVitalSign(85, 37.2f);
		WorkerHealth updated = workerHealthRepository.save(saved);

		Optional<WorkerHealth> result = workerHealthRepository.findById(updated.getId());
		assertThat(result).isPresent();
		assertThat(result.get().getHeartRate()).isEqualTo(85);
		assertThat(result.get().getBodyTemperature()).isEqualTo(37.2f);
	}
}
