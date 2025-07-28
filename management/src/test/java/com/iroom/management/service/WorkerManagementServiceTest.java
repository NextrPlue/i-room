package com.iroom.management.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

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
}