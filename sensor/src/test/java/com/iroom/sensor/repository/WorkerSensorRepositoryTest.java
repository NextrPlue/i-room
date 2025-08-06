package com.iroom.sensor.repository;

import com.iroom.sensor.entity.WorkerSensor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class WorkerSensorRepositoryTest {
	@Autowired
	private WorkerSensorRepository workerSensorRepository;

	@Test
	@DisplayName("근로자 센서 데이터 저장 후 ID로 조회 테스트")
	void saveAndFindById() {
		// given
		Double latitude = 35.8343;
		Double longitude = 128.4723;
		Double heartRate = 75.0;
		Long steps = 1000L;
		Double speed = 5.2;
		Double pace = 11.5;
		Long stepPerMinute = 120L;

		WorkerSensor workerSensor = WorkerSensor.builder()
			.workerId(1L)
			.build();
		workerSensor.updateSensor(latitude, longitude, heartRate, steps, speed, pace, stepPerMinute);

		// when
		WorkerSensor saved = workerSensorRepository.save(workerSensor);
		Optional<WorkerSensor> found = workerSensorRepository.findById(saved.getId());

		// then
		assertThat(found).isPresent();
		assertThat(found.get().getWorkerId()).isEqualTo(1L);
		assertThat(found.get().getLatitude()).isEqualTo(latitude);
		assertThat(found.get().getLongitude()).isEqualTo(longitude);
		assertThat(found.get().getHeartRate()).isEqualTo(heartRate);
		assertThat(found.get().getSteps()).isEqualTo(steps);
		assertThat(found.get().getSpeed()).isEqualTo(speed);
		assertThat(found.get().getPace()).isEqualTo(pace);
		assertThat(found.get().getStepPerMinute()).isEqualTo(stepPerMinute);
	}

	@Test
	@DisplayName("findByWorkerId 메서드 조회 테스트")
	void findByWorkerIdTest() {
		// given
		Double latitude = 35.8343;
		Double longitude = 128.4723;
		Double heartRate = 80.0;
		Long steps = 800L;
		Double speed = 4.1;
		Double pace = 14.6;
		Long stepPerMinute = 100L;

		WorkerSensor workerSensor = WorkerSensor.builder()
			.workerId(2L)
			.build();
		workerSensor.updateSensor(latitude, longitude, heartRate, steps, speed, pace, stepPerMinute);
		workerSensorRepository.save(workerSensor);

		// when
		Optional<WorkerSensor> result = workerSensorRepository.findByWorkerId(2L);

		// then
		assertThat(result).isPresent();
		assertThat(result.get().getLatitude()).isEqualTo(latitude);
		assertThat(result.get().getLongitude()).isEqualTo(longitude);
		assertThat(result.get().getHeartRate()).isEqualTo(heartRate);
		assertThat(result.get().getSteps()).isEqualTo(steps);
		assertThat(result.get().getSpeed()).isEqualTo(speed);
		assertThat(result.get().getPace()).isEqualTo(pace);
		assertThat(result.get().getStepPerMinute()).isEqualTo(stepPerMinute);
	}

	@Test
	@DisplayName("updateSensor 데이터 수정 테스트")
	void updateSensorAllDataTest() {
		// given
		Double latitude = 35.8343;
		Double longitude = 128.4723;
		Double heartRate = 75.0;
		Long steps = 1500L;
		Double speed = 6.3;
		Double pace = 9.5;
		Long stepPerMinute = 140L;

		WorkerSensor workerSensor = WorkerSensor.builder()
			.workerId(5L)
			.build();
		WorkerSensor saved = workerSensorRepository.save(workerSensor);

		// when
		saved.updateSensor(latitude, longitude, heartRate, steps, speed, pace, stepPerMinute);
		WorkerSensor updated = workerSensorRepository.save(saved);
		Optional<WorkerSensor> result = workerSensorRepository.findById(updated.getId());

		// then
		assertThat(result).isPresent();
		assertThat(result.get().getLatitude()).isEqualTo(latitude);
		assertThat(result.get().getLongitude()).isEqualTo(longitude);
		assertThat(result.get().getHeartRate()).isEqualTo(heartRate);
		assertThat(result.get().getSteps()).isEqualTo(steps);
		assertThat(result.get().getSpeed()).isEqualTo(speed);
		assertThat(result.get().getPace()).isEqualTo(pace);
		assertThat(result.get().getStepPerMinute()).isEqualTo(stepPerMinute);
	}
}