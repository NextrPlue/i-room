package com.iroom.management.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.iroom.management.dto.response.WorkerManagementResponse;
import com.iroom.management.dto.response.WorkerStatsResponse;
import com.iroom.management.entity.WorkerManagement;
import com.iroom.management.entity.WorkerReadModel;
import com.iroom.management.repository.WorkerManagementRepository;
import com.iroom.management.repository.WorkerReadModelRepository;
import com.iroom.modulecommon.exception.CustomException;
import com.iroom.modulecommon.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
class WorkerManagementServiceTest {

	@Mock
	private WorkerManagementRepository workerManagementRepository;

	@Mock
	private WorkerReadModelRepository workerReadModelRepository;

	@InjectMocks
	private WorkerManagementService service;

	private WorkerManagement worker;
	private WorkerReadModel workerReadModel;

	@BeforeEach
	void setUp() {
		worker = WorkerManagement.builder()
			.workerId(1L)
			.build();
		
		workerReadModel = WorkerReadModel.builder()
			.id(1L)
			.name("Test Worker")
			.build();
	}

	@Test
	@DisplayName("근로자 입장 기록 성공")
	void enterWorker_success() {
		// given
		given(workerReadModelRepository.findById(1L)).willReturn(Optional.of(workerReadModel));
		given(workerManagementRepository.findByWorkerIdAndOutDateIsNull(1L)).willReturn(Optional.empty());
		given(workerManagementRepository.save(any(WorkerManagement.class))).willReturn(worker);

		// when
		WorkerManagementResponse response = service.enterWorker(1L);

		// then
		assertThat(response.workerId()).isEqualTo(1L);
	}

	@Test
	@DisplayName("근로자 입장 실패 - workerId가 null인 경우")
	void enterWorker_fail_nullWorkerId() {
		assertThatThrownBy(() -> service.enterWorker(null))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.MANAGEMENT_INVALID_WORKER_ID);
	}

	@Test
	@DisplayName("근로자 퇴장 기록 성공")
	void exitWorker_success() {
		// given
		given(workerReadModelRepository.findById(1L)).willReturn(Optional.of(workerReadModel));
		given(workerManagementRepository.findByWorkerIdAndOutDateIsNull(1L)).willReturn(Optional.of(worker));
		given(workerManagementRepository.save(any(WorkerManagement.class))).willReturn(worker);

		// when
		WorkerManagementResponse response = service.exitWorker(1L);

		// then
		assertThat(response.workerId()).isEqualTo(1L);
	}

	@Test
	@DisplayName("근로자 퇴장 기록 실패 - 존재하지 않는 근로자")
	void exitWorker_fail_notFound() {
		// given
		given(workerReadModelRepository.findById(999L)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> service.exitWorker(999L))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.MANAGEMENT_WORKER_NOT_FOUND);
	}

	@Test
	@DisplayName("근로자 퇴장 기록 실패 - 이미 퇴장한 근로자")
	void exitWorker_fail_notEntered() {
		// given
		given(workerReadModelRepository.findById(1L)).willReturn(Optional.of(workerReadModel));
		given(workerManagementRepository.findByWorkerIdAndOutDateIsNull(1L)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> service.exitWorker(1L))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.MANAGEMENT_WORKER_NOT_ENTERED);
	}

	@Test
	@DisplayName("근로자 입장 실패 - 이미 입장한 근로자")
	void enterWorker_fail_alreadyEntered() {
		// given
		given(workerReadModelRepository.findById(1L)).willReturn(Optional.of(workerReadModel));
		given(workerManagementRepository.findByWorkerIdAndOutDateIsNull(1L)).willReturn(Optional.of(worker));

		// when & then
		assertThatThrownBy(() -> service.enterWorker(1L))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.MANAGEMENT_WORKER_ALREADY_ENTERED);
	}

	@Test
	@DisplayName("근로자 출입현황 조회 성공")
	void getEntryByWorkerId_success() {
		// given
		given(workerReadModelRepository.findById(1L)).willReturn(Optional.of(workerReadModel));
		given(workerManagementRepository.findTopByWorkerIdOrderByEnterDateDesc(1L)).willReturn(Optional.of(worker));

		// when
		WorkerManagementResponse response = service.getEntryByWorkerId(1L);

		// then
		assertThat(response.workerId()).isEqualTo(1L);
	}

	@Test
	@DisplayName("근로자 출입현황 조회 성공 - 출입기록 없음")
	void getEntryByWorkerId_noRecord() {
		// given
		WorkerReadModel workerReadModel999 = WorkerReadModel.builder()
			.id(999L)
			.name("Test Worker 999")
			.build();
		given(workerReadModelRepository.findById(999L)).willReturn(Optional.of(workerReadModel999));
		given(workerManagementRepository.findTopByWorkerIdOrderByEnterDateDesc(999L)).willReturn(Optional.empty());

		// when
		WorkerManagementResponse response = service.getEntryByWorkerId(999L);

		// then
		assertThat(response.workerId()).isEqualTo(999L);
		assertThat(response.id()).isNull();
		assertThat(response.enterDate()).isNull();
		assertThat(response.outDate()).isNull();
	}
	
	@Test
	@DisplayName("근로자 통계 조회 성공")
	void getWorkerStatistics_success() {
		// given
		given(workerReadModelRepository.count()).willReturn(3L);
		
		// Mock WorkerManagement 객체들 
		WorkerManagement working = mock(WorkerManagement.class);
		WorkerManagement finished = mock(WorkerManagement.class);
		
		LocalDateTime today = LocalDateTime.now().toLocalDate().atStartOfDay();
		given(working.getOutDate()).willReturn(null); // 아직 퇴근 안함
		given(finished.getOutDate()).willReturn(today.plusHours(18)); // 퇴근함
		
		List<WorkerManagement> todayEntries = List.of(working, finished);
		Page<WorkerManagement> todayPage = new PageImpl<>(todayEntries);
		
		given(workerManagementRepository.findByEnterDateBetween(
			any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
			.willReturn(todayPage);

		// when
		WorkerStatsResponse stats = service.getWorkerStatistics();

		// then
		assertThat(stats.total()).isEqualTo(3);      // 전체 3명
		assertThat(stats.working()).isEqualTo(1);    // 근무중 1명
		assertThat(stats.offWork()).isEqualTo(1);    // 퇴근 1명  
		assertThat(stats.absent()).isEqualTo(1);     // 미출근 1명
	}

	@Test
	@DisplayName("근로자 통계 조회 성공 - 출입 기록 없음")
	void getWorkerStatistics_noEntries() {
		// given
		given(workerReadModelRepository.count()).willReturn(5L);
		given(workerManagementRepository.findByEnterDateBetween(
			any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
			.willReturn(new PageImpl<>(List.of()));

		// when
		WorkerStatsResponse stats = service.getWorkerStatistics();

		// then
		assertThat(stats.total()).isEqualTo(5);
		assertThat(stats.working()).isEqualTo(0);
		assertThat(stats.offWork()).isEqualTo(0);
		assertThat(stats.absent()).isEqualTo(5);
	}

	@Test
	@DisplayName("근로자 통계 조회 성공 - 모든 근로자가 근무중")
	void getWorkerStatistics_allWorking() {
		// given  
		given(workerReadModelRepository.count()).willReturn(2L);
		
		WorkerManagement worker1 = mock(WorkerManagement.class);
		WorkerManagement worker2 = mock(WorkerManagement.class);
		
		given(worker1.getOutDate()).willReturn(null); // 퇴근 안함
		given(worker2.getOutDate()).willReturn(null); // 퇴근 안함
		
		List<WorkerManagement> todayEntries = List.of(worker1, worker2);
		Page<WorkerManagement> todayPage = new PageImpl<>(todayEntries);
		
		given(workerManagementRepository.findByEnterDateBetween(
			any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
			.willReturn(todayPage);

		// when
		WorkerStatsResponse stats = service.getWorkerStatistics();

		// then
		assertThat(stats.total()).isEqualTo(2);
		assertThat(stats.working()).isEqualTo(2);
		assertThat(stats.offWork()).isEqualTo(0);
		assertThat(stats.absent()).isEqualTo(0);
	}

	@Test
	@DisplayName("근로자 본인 출입현황 조회 성공")
	void getWorkerEntry_success() {
		// given
		given(workerManagementRepository.findById(1L)).willReturn(Optional.of(worker));

		// when
		WorkerManagementResponse response = service.getWorkerEntry(1L);

		// then
		assertThat(response.workerId()).isEqualTo(1L);
	}
}