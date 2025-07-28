package com.iroom.management.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.iroom.management.entity.WorkerManagement;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class WorkerManagementRepositoryTest {

	@Autowired
	private WorkerManagementRepository workerManagementRepository;

	private WorkerManagement workerManagement;

	@BeforeEach
	void setUp() {
		workerManagementRepository.deleteAll();

		workerManagement = WorkerManagement.builder()
			.workerId(1L)
			.build();

		workerManagementRepository.save(workerManagement);
	}

	@Test
	@DisplayName("근로자 출입 기록 저장 및 조회 성공")
	void saveAndFindById() {
		// when
		Optional<WorkerManagement> result = workerManagementRepository.findById(workerManagement.getId());

		// then
		assertThat(result).isPresent();
		assertThat(result.get().getWorkerId()).isEqualTo(1L);
	}

	@Test
	@DisplayName("근로자 출입 기록 삭제 성공")
	void deleteWorkerManagement() {
		// when
		workerManagementRepository.delete(workerManagement);

		// then
		Optional<WorkerManagement> result = workerManagementRepository.findById(workerManagement.getId());
		assertThat(result).isNotPresent();
	}

	@Test
	@DisplayName("workerId로 조회 성공")
	void findByWorkerId() {
		// when
		Optional<WorkerManagement> result = workerManagementRepository.findByWorkerId(1L);

		// then
		assertThat(result).isPresent();
		assertThat(result.get().getEnterDate()).isNotNull();
	}
}