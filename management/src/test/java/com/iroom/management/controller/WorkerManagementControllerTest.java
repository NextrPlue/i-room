package com.iroom.management.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;

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
}
