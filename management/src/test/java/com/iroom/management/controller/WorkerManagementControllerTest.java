package com.iroom.management.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iroom.management.dto.response.WorkerManagementResponse;
import com.iroom.management.dto.response.WorkerStatsResponse;
import com.iroom.management.dto.response.WorkingWorkerResponse;
import com.iroom.management.service.WorkerManagementService;
import com.iroom.modulecommon.dto.response.PagedResponse;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@WebMvcTest(value = WorkerManagementController.class, excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class})
@Import(WorkerManagementControllerTest.MockConfig.class)
class WorkerManagementControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private WorkerManagementService workerManagementService;

	@Autowired
	private ObjectMapper objectMapper;

	@TestConfiguration
	static class MockConfig {
		@Bean
		public WorkerManagementService workerManagementService() {
			return Mockito.mock(WorkerManagementService.class);
		}
	}

	@Test
	@DisplayName("근로자 체크인 성공")
	void checkInTest() throws Exception {
		// given
		WorkerManagementResponse response = new WorkerManagementResponse(
			1L,
			10L,
			LocalDateTime.of(2025, 7, 28, 10, 0),
			null
		);

		Mockito.when(workerManagementService.enterWorker(anyLong()))
			.thenReturn(response);

		// when & then
		mockMvc.perform(post("/entries/10/check-in")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.workerId").value(10L))
			.andExpect(jsonPath("$.data.enterDate").value("2025-07-28T10:00:00"));
	}

	@Test
	@DisplayName("근로자 체크아웃 성공")
	void checkOutTest() throws Exception {
		// given
		WorkerManagementResponse response = new WorkerManagementResponse(
			2L,
			10L,
			LocalDateTime.of(2025, 7, 28, 9, 0),
			LocalDateTime.of(2025, 7, 28, 18, 0)
		);

		Mockito.when(workerManagementService.exitWorker(anyLong()))
			.thenReturn(response);

		// when & then
		mockMvc.perform(post("/entries/10/check-out")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.workerId").value(10L))
			.andExpect(jsonPath("$.data.enterDate").value("2025-07-28T09:00:00"))
			.andExpect(jsonPath("$.data.outDate").value("2025-07-28T18:00:00"));
	}

	@Test
	@DisplayName("근로자 출입현황 조회 성공")
	void getEntryTest() throws Exception {
		// given
		WorkerManagementResponse response = new WorkerManagementResponse(
			3L,
			10L,
			LocalDateTime.of(2025, 7, 28, 9, 0),
			null
		);

		Mockito.when(workerManagementService.getEntryByWorkerId(anyLong()))
			.thenReturn(response);

		// when & then
		mockMvc.perform(get("/entries/10")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.workerId").value(10L))
			.andExpect(jsonPath("$.data.enterDate").value("2025-07-28T09:00:00"))
			.andExpect(jsonPath("$.data.outDate").doesNotExist());
	}

	@Test
	@DisplayName("근로자 출입현황 목록 조회 성공 - 날짜 조건 없음")
	void getEntriesTest() throws Exception {
		// given
		WorkerManagementResponse res1 = new WorkerManagementResponse(1L, 10L, 
			LocalDateTime.of(2025, 7, 28, 9, 0), LocalDateTime.of(2025, 7, 28, 18, 0));
		WorkerManagementResponse res2 = new WorkerManagementResponse(2L, 11L, 
			LocalDateTime.of(2025, 7, 28, 10, 0), null);
		List<WorkerManagementResponse> content = List.of(res1, res2);

		PagedResponse<WorkerManagementResponse> pagedResponse = PagedResponse.of(
			new PageImpl<>(content, PageRequest.of(0, 10), content.size()));

		Mockito.when(workerManagementService.getEntries(null, 0, 10))
			.thenReturn(pagedResponse);

		// when & then
		mockMvc.perform(get("/entries?page=0&size=10")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.content.length()").value(2))
			.andExpect(jsonPath("$.data.content[0].workerId").value(10L))
			.andExpect(jsonPath("$.data.content[1].workerId").value(11L));
	}

	@Test
	@DisplayName("근로자 출입현황 목록 조회 성공 - 날짜 조건 있음")
	void getEntriesWithDateTest() throws Exception {
		// given
		WorkerManagementResponse response = new WorkerManagementResponse(1L, 10L, 
			LocalDateTime.of(2025, 7, 28, 9, 0), LocalDateTime.of(2025, 7, 28, 18, 0));
		List<WorkerManagementResponse> content = List.of(response);

		PagedResponse<WorkerManagementResponse> pagedResponse = PagedResponse.of(
			new PageImpl<>(content, PageRequest.of(0, 10), content.size()));

		Mockito.when(workerManagementService.getEntries("2025-07-28", 0, 10))
			.thenReturn(pagedResponse);

		// when & then
		mockMvc.perform(get("/entries?date=2025-07-28&page=0&size=10")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.content.length()").value(1))
			.andExpect(jsonPath("$.data.content[0].workerId").value(10L));
	}

	@Test
	@DisplayName("근로자 통계 조회 성공")
	void getWorkerStatsTest() throws Exception {
		// given
		WorkerStatsResponse response = new WorkerStatsResponse(10, 5, 3, 2);

		Mockito.when(workerManagementService.getWorkerStatistics())
			.thenReturn(response);

		// when & then
		mockMvc.perform(get("/entries/statistics")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.total").value(10))
			.andExpect(jsonPath("$.data.working").value(5))
			.andExpect(jsonPath("$.data.offWork").value(3))
			.andExpect(jsonPath("$.data.absent").value(2));
	}

	@Test
	@DisplayName("근로자 본인 출입현황 조회 성공")
	void getMyInfoTest() throws Exception {
		// given
		Long workerId = 5L;
		WorkerManagementResponse response = new WorkerManagementResponse(
			4L,
			workerId,
			LocalDateTime.of(2025, 7, 28, 8, 30),
			LocalDateTime.of(2025, 7, 28, 17, 30)
		);

		Mockito.when(workerManagementService.getWorkerEntry(anyLong()))
			.thenReturn(response);

		// when & then
		mockMvc.perform(get("/entries/me")
				.header("X-User-Id", workerId)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.id").value(4L))
			.andExpect(jsonPath("$.data.workerId").value(workerId))
			.andExpect(jsonPath("$.data.enterDate").value("2025-07-28T08:30:00"))
			.andExpect(jsonPath("$.data.outDate").value("2025-07-28T17:30:00"));
	}

	@Test
	@DisplayName("근무중인 근로자 목록 조회 성공")
	void getWorkingWorkersTest() throws Exception {
		// given
		WorkingWorkerResponse worker1 = new WorkingWorkerResponse(
			1L, "김철수", "건설부", "철근공", 
			LocalDateTime.of(2025, 7, 28, 8, 30)
		);
		WorkingWorkerResponse worker2 = new WorkingWorkerResponse(
			2L, "이영희", "안전관리부", "안전관리자", 
			LocalDateTime.of(2025, 7, 28, 9, 0)
		);

		List<WorkingWorkerResponse> workingWorkers = Arrays.asList(worker1, worker2);

		Mockito.when(workerManagementService.getWorkingWorkers())
			.thenReturn(workingWorkers);

		// when & then
		mockMvc.perform(get("/entries/working-workers")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("success"))
			.andExpect(jsonPath("$.data.length()").value(2))
			.andExpect(jsonPath("$.data[0].workerId").value(1L))
			.andExpect(jsonPath("$.data[0].workerName").value("김철수"))
			.andExpect(jsonPath("$.data[0].department").value("건설부"))
			.andExpect(jsonPath("$.data[0].occupation").value("철근공"))
			.andExpect(jsonPath("$.data[0].enterDate").value("2025-07-28T08:30:00"))
			.andExpect(jsonPath("$.data[1].workerId").value(2L))
			.andExpect(jsonPath("$.data[1].workerName").value("이영희"))
			.andExpect(jsonPath("$.data[1].department").value("안전관리부"))
			.andExpect(jsonPath("$.data[1].occupation").value("안전관리자"))
			.andExpect(jsonPath("$.data[1].enterDate").value("2025-07-28T09:00:00"));
	}

	@Test
	@DisplayName("근무중인 근로자 목록 조회 - 근로자 없음")
	void getWorkingWorkers_emptyTest() throws Exception {
		// given
		Mockito.when(workerManagementService.getWorkingWorkers())
			.thenReturn(Arrays.asList());

		// when & then
		mockMvc.perform(get("/entries/working-workers")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("success"))
			.andExpect(jsonPath("$.data.length()").value(0));
	}
}
