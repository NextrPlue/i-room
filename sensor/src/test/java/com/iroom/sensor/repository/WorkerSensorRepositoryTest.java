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
		Integer heartRate = 75;
		
		WorkerSensor workerSensor = WorkerSensor.builder()
			.workerId(1L)
			.build();
		workerSensor.updateSensor(latitude, longitude, heartRate);

		// when
		WorkerSensor saved = workerSensorRepository.save(workerSensor);
		Optional<WorkerSensor> found = workerSensorRepository.findById(saved.getId());

		// then
		assertThat(found).isPresent();
		assertThat(found.get().getWorkerId()).isEqualTo(1L);
		assertThat(found.get().getLatitude()).isEqualTo(latitude);
		assertThat(found.get().getLongitude()).isEqualTo(longitude);
		assertThat(found.get().getHeartRate()).isEqualTo(heartRate);
	}

	@Test
	@DisplayName("findByWorkerId 메서드 조회 테스트")
	void findByWorkerIdTest() {
		// given
		Double latitude = 35.8343;
		Double longitude = 128.4723;
		Integer heartRate = 80;
		
		WorkerSensor workerSensor = WorkerSensor.builder()
			.workerId(2L)
			.build();
		workerSensor.updateSensor(latitude, longitude, heartRate);
		workerSensorRepository.save(workerSensor);

		// when
		Optional<WorkerSensor> result = workerSensorRepository.findByWorkerId(2L);

		// then
		assertThat(result).isPresent();
		assertThat(result.get().getLatitude()).isEqualTo(latitude);
		assertThat(result.get().getLongitude()).isEqualTo(longitude);
		assertThat(result.get().getHeartRate()).isEqualTo(heartRate);
	}

	@Test
	@DisplayName("updateSensor 메서드 위치 데이터 수정 테스트")
	void updateSensorLocationTest() {
		// given
		Double initialLatitude = 35.8343;
		Double initialLongitude = 128.4723;
		Double newLatitude = 37.5665;
		Double newLongitude = 126.9780;
		
		WorkerSensor workerSensor = WorkerSensor.builder()
			.workerId(3L)
			.build();
		workerSensor.updateSensor(initialLatitude, initialLongitude, null);
		WorkerSensor saved = workerSensorRepository.save(workerSensor);

		// when
		saved.updateSensor(newLatitude, newLongitude, null);
		WorkerSensor updated = workerSensorRepository.save(saved);
		Optional<WorkerSensor> result = workerSensorRepository.findById(updated.getId());

		// then
		assertThat(result).isPresent();
		assertThat(result.get().getLatitude()).isEqualTo(newLatitude);
		assertThat(result.get().getLongitude()).isEqualTo(newLongitude);
	}

	@Test
	@DisplayName("updateSensor 메서드 심박수 데이터 수정 테스트")
	void updateSensorHeartRateTest() {
		// given
		Integer initialHeartRate = 70;
		Integer newHeartRate = 85;
		
		WorkerSensor workerSensor = WorkerSensor.builder()
			.workerId(4L)
			.build();
		workerSensor.updateSensor(null, null, initialHeartRate);
		WorkerSensor saved = workerSensorRepository.save(workerSensor);

		// when
		saved.updateSensor(null, null, newHeartRate);
		WorkerSensor updated = workerSensorRepository.save(saved);
		Optional<WorkerSensor> result = workerSensorRepository.findById(updated.getId());

		// then
		assertThat(result).isPresent();
		assertThat(result.get().getHeartRate()).isEqualTo(newHeartRate);
	}

	@Test
	@DisplayName("updateSensor 메서드 모든 데이터 통합 수정 테스트")
	void updateSensorAllDataTest() {
		// given
		Double latitude = 35.8343;
		Double longitude = 128.4723;
		Integer heartRate = 75;
		
		WorkerSensor workerSensor = WorkerSensor.builder()
			.workerId(5L)
			.build();
		WorkerSensor saved = workerSensorRepository.save(workerSensor);

		// when
		saved.updateSensor(latitude, longitude, heartRate);
		WorkerSensor updated = workerSensorRepository.save(saved);
		Optional<WorkerSensor> result = workerSensorRepository.findById(updated.getId());

		// then
		assertThat(result).isPresent();
		assertThat(result.get().getLatitude()).isEqualTo(latitude);
		assertThat(result.get().getLongitude()).isEqualTo(longitude);
		assertThat(result.get().getHeartRate()).isEqualTo(heartRate);
	}
}