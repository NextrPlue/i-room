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
import com.iroom.management.repository.WorkerManagementRepository;

@ExtendWith(MockitoExtension.class)
class WorkerManagementServiceTest {

	@Mock
	private WorkerManagementRepository repository;

	@InjectMocks
	private WorkerManagementServiceImpl service;

	private WorkerManagement worker;

	@BeforeEach
	void setUp() {
		worker = WorkerManagement.builder()
			.workerId(1L)
			.build();
	}

	@Test
	@DisplayName("근로자 입장 기록 성공")
	void enterWorker_success() {
		// given
		given(repository.save(any(WorkerManagement.class))).willReturn(worker);

		// when
		WorkerManagementResponse response = service.enterWorker(1L);

		// then
		assertThat(response.workerId()).isEqualTo(1L);
	}

	@Test
	@DisplayName("근로자 입장 실패 - workerId가 null인 경우")
	void enterWorker_fail_nullWorkerId() {
		assertThatThrownBy(() -> service.enterWorker(null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("workerId는 null일 수 없습니다.");
	}

	@Test
	@DisplayName("근로자 퇴장 기록 성공")
	void exitWorker_success() {
		// given
		given(repository.findByWorkerId(1L)).willReturn(Optional.of(worker));
		given(repository.save(any(WorkerManagement.class))).willReturn(worker);

		// when
		WorkerManagementResponse response = service.exitWorker(1L);

		// then
		assertThat(response.workerId()).isEqualTo(1L);
		assertThat(response.outDate()).isNotNull();
	}

	@Test
	@DisplayName("근로자 퇴장 기록 실패 - 존재하지 않는 근로자")
	void exitWorker_fail_notFound() {
		// given
		given(repository.findByWorkerId(999L)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> service.exitWorker(999L))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("해당 근로자를 찾을 수 없습니다.");
	}
}