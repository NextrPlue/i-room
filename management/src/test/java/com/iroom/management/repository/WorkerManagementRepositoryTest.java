package com.iroom.management.repository;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

	@Test
	@DisplayName("날짜 범위로 출입 기록 조회 성공")
	void findByEnterDateBetween() {
		// given
		LocalDateTime startDate = LocalDateTime.of(2025, 8, 11, 0, 0);
		LocalDateTime endDate = LocalDateTime.of(2025, 8, 11, 23, 59);
		Pageable pageable = PageRequest.of(0, 10);

		// 추가 테스트 데이터 생성 - 날짜 범위에 포함
		WorkerManagement todayEntry = WorkerManagement.builder()
			.workerId(2L)
			.build();
		workerManagementRepository.save(todayEntry);

		// when
		Page<WorkerManagement> result = workerManagementRepository.findByEnterDateBetween(startDate, endDate, pageable);

		// then
		assertThat(result.getContent()).isNotEmpty();
		assertThat(result.getContent().size()).isGreaterThanOrEqualTo(1);
		// 모든 기록이 지정된 날짜 범위 내에 있는지 확인
		result.getContent().forEach(entry -> {
			assertThat(entry.getEnterDate()).isAfterOrEqualTo(startDate);
			assertThat(entry.getEnterDate()).isBeforeOrEqualTo(endDate);
		});
	}

	@Test
	@DisplayName("날짜 범위로 출입 기록 조회 - 범위 밖 데이터는 조회되지 않음")
	void findByEnterDateBetween_outsideRange() {
		// given
		LocalDateTime startDate = LocalDateTime.of(2025, 8, 10, 0, 0);
		LocalDateTime endDate = LocalDateTime.of(2025, 8, 10, 23, 59);
		Pageable pageable = PageRequest.of(0, 10);

		// when - 어제 날짜로 조회
		Page<WorkerManagement> result = workerManagementRepository.findByEnterDateBetween(startDate, endDate, pageable);

		// then - 오늘 생성된 데이터는 조회되지 않음
		assertThat(result.getContent()).isEmpty();
	}
}