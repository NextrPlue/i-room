package com.iroom.management.service;

import com.iroom.modulecommon.dto.response.PagedResponse;
import com.iroom.modulecommon.exception.CustomException;
import com.iroom.modulecommon.exception.ErrorCode;
import com.iroom.management.dto.request.WorkerEduRequest;
import com.iroom.management.dto.response.WorkerEduResponse;
import com.iroom.management.entity.WorkerEdu;
import com.iroom.management.entity.WorkerReadModel;
import com.iroom.management.repository.WorkerEduRepository;
import com.iroom.management.repository.WorkerReadModelRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class WorkerEduServiceTest {

	@Mock
	private WorkerEduRepository workerEduRepository;

	@Mock
	private WorkerReadModelRepository workerReadModelRepository;

	@InjectMocks
	private WorkerEduService workerEduService;

	private WorkerEduRequest request;
	private WorkerEdu workerEdu;
	private WorkerReadModel workerReadModel;
	private Pageable pageable;

	@BeforeEach
	void setUp() {
		request = new WorkerEduRequest(
			null,
			1L,
			"근로자1",
			"https://example.com/cert1.jpg",
			LocalDate.of(2024, 7, 1)
		);

		workerEdu = WorkerEdu.builder()
			.workerId(1L)
			.name("근로자1")
			.certUrl("https://example.com/cert1.jpg")
			.eduDate(LocalDate.of(2024, 7, 1))
			.build();

		workerReadModel = WorkerReadModel.builder()
			.id(1L)
			.name("근로자1")
			.email("worker1@test.com")
			.build();

		pageable = PageRequest.of(0, 10);
	}

	@Test
	@DisplayName("교육 이력 등록 성공")
	void recordEdu_success() {
		// given
		given(workerReadModelRepository.findById(1L)).willReturn(Optional.of(workerReadModel));
		given(workerEduRepository.save(any(WorkerEdu.class))).willReturn(workerEdu);

		// when
		WorkerEduResponse response = workerEduService.recordEdu(request);

		// then
		assertThat(response.workerId()).isEqualTo(1L);
		assertThat(response.name()).isEqualTo("근로자1");
		assertThat(response.certUrl()).isEqualTo("https://example.com/cert1.jpg");
		verify(workerReadModelRepository).findById(1L);
		verify(workerEduRepository).save(any(WorkerEdu.class));
	}

	@Test
	@DisplayName("교육 이력 조회 성공")
	void getEduInfo_success() {
		// given
		given(workerReadModelRepository.findById(1L)).willReturn(Optional.of(workerReadModel));
		List<WorkerEdu> eduList = List.of(workerEdu);
		Page<WorkerEdu> page = new PageImpl<>(eduList, pageable, eduList.size());
		given(workerEduRepository.findAllByWorkerId(1L, pageable)).willReturn(page);

		// when
		PagedResponse<WorkerEduResponse> response = workerEduService.getEduInfo(1L, 0, 10);

		// then
		assertThat(response.content()).hasSize(1);
		assertThat(response.totalElements()).isEqualTo(1);
		assertThat(response.content().get(0).workerId()).isEqualTo(1L);
		verify(workerReadModelRepository).findById(1L);
		verify(workerEduRepository).findAllByWorkerId(1L, pageable);
	}

	@Test
	@DisplayName("교육 이력 등록 실패 - workerId null")
	void recordEdu_fail_nullWorkerId() {
		// given
		WorkerEduRequest invalidRequest = new WorkerEduRequest(
			null, null, "근로자1", "https://example.com/cert1.jpg", LocalDate.now()
		);

		// when & then
		assertThatThrownBy(() -> workerEduService.recordEdu(invalidRequest))
			.isInstanceOf(CustomException.class)
			.hasMessage(ErrorCode.MANAGEMENT_INVALID_WORKER_ID.getMessage());
	}

	@Test
	@DisplayName("교육 이력 등록 실패 - 존재하지 않는 근로자")
	void recordEdu_fail_workerNotFound() {
		// given
		given(workerReadModelRepository.findById(999L)).willReturn(Optional.empty());

		WorkerEduRequest invalidRequest = new WorkerEduRequest(
			null, 999L, "근로자999", "https://example.com/cert999.jpg", LocalDate.now()
		);

		// when & then
		assertThatThrownBy(() -> workerEduService.recordEdu(invalidRequest))
			.isInstanceOf(CustomException.class)
			.hasMessage(ErrorCode.MANAGEMENT_WORKER_NOT_FOUND.getMessage());
		
		verify(workerReadModelRepository).findById(999L);
		verify(workerEduRepository, never()).save(any());
	}

	@Test
	@DisplayName("교육 이력 조회 실패 - workerId null")
	void getEduInfo_fail_nullWorkerId() {
		// when & then
		assertThatThrownBy(() -> workerEduService.getEduInfo(null, 0, 10))
			.isInstanceOf(CustomException.class)
			.hasMessage(ErrorCode.MANAGEMENT_INVALID_WORKER_ID.getMessage());
	}

	@Test
	@DisplayName("교육 이력 조회 실패 - 존재하지 않는 근로자")
	void getEduInfo_fail_workerNotFound() {
		// given
		given(workerReadModelRepository.findById(999L)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> workerEduService.getEduInfo(999L, 0, 10))
			.isInstanceOf(CustomException.class)
			.hasMessage(ErrorCode.MANAGEMENT_WORKER_NOT_FOUND.getMessage());
		
		verify(workerReadModelRepository).findById(999L);
		verify(workerEduRepository, never()).findAllByWorkerId(anyLong(), any(Pageable.class));
	}
}