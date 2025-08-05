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

	@Test
	@DisplayName("퇴장하지 않은 근로자 조회 성공")
	void findByWorkerIdAndOutDateIsNull() {
		// when
		Optional<WorkerManagement> result = workerManagementRepository.findByWorkerIdAndOutDateIsNull(1L);

		// then
		assertThat(result).isPresent();
		assertThat(result.get().getWorkerId()).isEqualTo(1L);
		assertThat(result.get().getOutDate()).isNull();
	}

	@Test
	@DisplayName("퇴장한 근로자는 조회되지 않음")
	void findByWorkerIdAndOutDateIsNull_exitedWorker() {
		// given
		workerManagement.markExitedNow();
		workerManagementRepository.save(workerManagement);

		// when
		Optional<WorkerManagement> result = workerManagementRepository.findByWorkerIdAndOutDateIsNull(1L);

		// then
		assertThat(result).isNotPresent();
	}

	@Test
	@DisplayName("가장 최근 출입 기록 조회 성공")
	void findTopByWorkerIdOrderByEnterDateDesc() {
		// given - 동일한 근로자의 추가 출입 기록 생성
		WorkerManagement secondEntry = WorkerManagement.builder()
			.workerId(1L)
			.build();
		workerManagementRepository.save(secondEntry);

		// when
		Optional<WorkerManagement> result = workerManagementRepository.findTopByWorkerIdOrderByEnterDateDesc(1L);

		// then
		assertThat(result).isPresent();
		assertThat(result.get().getWorkerId()).isEqualTo(1L);
		// 가장 최근 기록이 조회되어야 함
		assertThat(result.get().getEnterDate()).isAfterOrEqualTo(workerManagement.getEnterDate());
	}
}