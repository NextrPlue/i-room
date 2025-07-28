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
import com.iroom.management.service.WorkerManagementService;

@WebMvcTest(WorkerManagementController.class)
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
			.andExpect(jsonPath("$.workerId").value(10L))
			.andExpect(jsonPath("$.enterDate").value("2025-07-28T10:00:00"));
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
			.andExpect(jsonPath("$.workerId").value(10L))
			.andExpect(jsonPath("$.enterDate").value("2025-07-28T09:00:00"))
			.andExpect(jsonPath("$.outDate").value("2025-07-28T18:00:00"));
	}
}
