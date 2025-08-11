package com.iroom.management.service;

import com.iroom.modulecommon.dto.response.PagedResponse;
import com.iroom.management.dto.request.WorkerEduRequest;
import com.iroom.management.dto.response.WorkerEduResponse;
import com.iroom.management.entity.WorkerEdu;
import com.iroom.management.repository.WorkerEduRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

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

	@InjectMocks
	private WorkerEduService workerEduService;

	private WorkerEduRequest request;
	private WorkerEdu workerEdu;
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

		pageable = PageRequest.of(0, 10);
	}

	@Test
	@DisplayName("교육 이력 등록 성공")
	void recordEdu_success() {
		// given
		given(workerEduRepository.save(any(WorkerEdu.class))).willReturn(workerEdu);

		// when
		WorkerEduResponse response = workerEduService.recordEdu(request);

		// then
		assertThat(response.workerId()).isEqualTo(1L);
		assertThat(response.name()).isEqualTo("근로자1");
		assertThat(response.certUrl()).isEqualTo("https://example.com/cert1.jpg");
	}

	@Test
	@DisplayName("교육 이력 조회 성공")
	void getEduInfo_success() {
		// given
		List<WorkerEdu> eduList = List.of(workerEdu);
		Page<WorkerEdu> page = new PageImpl<>(eduList, pageable, eduList.size());
		given(workerEduRepository.findAllByWorkerId(1L, pageable)).willReturn(page);

		// when
		PagedResponse<WorkerEduResponse> response = workerEduService.getEduInfo(1L, 0, 10);

		// then
		assertThat(response.content()).hasSize(1);
		assertThat(response.totalElements()).isEqualTo(1);
		assertThat(response.content().get(0).workerId()).isEqualTo(1L);
	}

	@Test
	@DisplayName("교육 이력 조회 실패 - 존재하지 않는 근로자")
	void getEduInfo_fail_empty() {
		// given
		Page<WorkerEdu> emptyPage = new PageImpl<>(List.of(), pageable, 0);
		given(workerEduRepository.findAllByWorkerId(999L, pageable)).willReturn(emptyPage);

		// when
		PagedResponse<WorkerEduResponse> response = workerEduService.getEduInfo(999L, 0, 10);

		// then
		assertThat(response.content()).isEmpty();
		assertThat(response.totalElements()).isZero();
	}

	@Test
	@DisplayName("근로자 본인 안전교육 내역 조회 성공")
	void getWorkerEdu_success() {
		// given
		List<WorkerEdu> eduList = List.of(workerEdu);
		Page<WorkerEdu> page = new PageImpl<>(eduList, pageable, eduList.size());
		given(workerEduRepository.findAllByWorkerId(1L, pageable)).willReturn(page);

		// when
		PagedResponse<WorkerEduResponse> response = workerEduService.getWorkerEdu(1L, 0, 10);

		// then
		assertThat(response.content()).hasSize(1);
		assertThat(response.totalElements()).isEqualTo(1);
		assertThat(response.content().get(0).workerId()).isEqualTo(1L);
		assertThat(response.content().get(0).name()).isEqualTo("근로자1");
		assertThat(response.content().get(0).certUrl()).isEqualTo("https://example.com/cert1.jpg");
	}
}