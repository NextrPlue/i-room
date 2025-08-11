package com.iroom.management.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.iroom.management.dto.response.WorkerManagementResponse;
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