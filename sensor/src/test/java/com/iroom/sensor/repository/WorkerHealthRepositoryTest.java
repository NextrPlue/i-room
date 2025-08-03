package com.iroom.sensor.repository;

import com.iroom.sensor.entity.WorkerHealth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class WorkerHealthRepositoryTest {
	@Autowired
	private WorkerHealthRepository workerHealthRepository;

	@Test
	@DisplayName("근로자 생체정보 저장 후 ID로 조회 테스트")
	void saveAndFindById() {
		// given
		Double latitude = 35.8343;
		Double longitude = 128.4723;
		WorkerHealth health = WorkerHealth.builder()
			.workerId(1L)
			.build();
		health.updateLocation(latitude, longitude);
		health.updateVitalSign(75, 36.5f);

		// when
		WorkerHealth saved = workerHealthRepository.save(health);
		Optional<WorkerHealth> found = workerHealthRepository.findById(saved.getId());

		// then
		assertThat(found).isPresent();
		assertThat(found.get().getWorkerId()).isEqualTo(1L);
		assertThat(found.get().getLatitude()).isEqualTo(latitude);
		assertThat(found.get().getLongitude()).isEqualTo(longitude);
		assertThat(found.get().getHeartRate()).isEqualTo(75);
		assertThat(found.get().getBodyTemperature()).isEqualTo(36.5f);
	}

	@Test
	@DisplayName("findByWorkerId 메서드 조회 테스트")
	void findByWorkerIdTest() {
		// given
		Double latitude = 35.8343;
		Double longitude = 128.4723;
		WorkerHealth health = WorkerHealth.builder()
			.workerId(2L)
			.build();
		health.updateLocation(latitude, longitude);
		health.updateVitalSign(80, 36.8f);
		workerHealthRepository.save(health);

		// when
		Optional<WorkerHealth> result = workerHealthRepository.findByWorkerId(2L);

		// then
		assertThat(result).isPresent();
		assertThat(result.get().getLatitude()).isEqualTo(latitude);
		assertThat(result.get().getLongitude()).isEqualTo(longitude);
		assertThat(result.get().getHeartRate()).isEqualTo(80);
		assertThat(result.get().getBodyTemperature()).isEqualTo(36.8f);
	}

	@Test
	@DisplayName("updateLocation 메서드 위치 수정 테스트")
	void updateLocationTest() {
		// given
		Double latitude = 35.8343;
		Double longitude = 128.4723;
		WorkerHealth health = WorkerHealth.builder()
			.workerId(3L)
			.build();
		health.updateLocation(latitude, longitude);
		WorkerHealth saved = workerHealthRepository.save(health);

		// when
		saved.updateLocation(latitude, longitude);
		WorkerHealth updated = workerHealthRepository.save(saved);
		Optional<WorkerHealth> result = workerHealthRepository.findById(updated.getId());

		// then
		assertThat(result).isPresent();
		assertThat(result.get().getLatitude()).isEqualTo(latitude);
		assertThat(result.get().getLongitude()).isEqualTo(longitude);
	}

	@Test
	@DisplayName("updateVitalSign 메서드 생체정보 수정 테스트")
	void updateVitalSignTest() {
		// given
		WorkerHealth health = WorkerHealth.builder()
			.workerId(4L)
			.build();
		health.updateVitalSign(70, 36.0f);
		WorkerHealth saved = workerHealthRepository.save(health);

		// when
		saved.updateVitalSign(85, 37.2f);
		WorkerHealth updated = workerHealthRepository.save(saved);
		Optional<WorkerHealth> result = workerHealthRepository.findById(updated.getId());

		// then
		assertThat(result).isPresent();
		assertThat(result.get().getHeartRate()).isEqualTo(85);
		assertThat(result.get().getBodyTemperature()).isEqualTo(37.2f);
	}
}
