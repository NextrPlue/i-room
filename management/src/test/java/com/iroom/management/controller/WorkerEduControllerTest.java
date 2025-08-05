package com.iroom.management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iroom.modulecommon.dto.response.PagedResponse;
import com.iroom.management.dto.request.WorkerEduRequest;
import com.iroom.management.dto.response.WorkerEduResponse;
import com.iroom.management.service.WorkerEduService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = WorkerEduController.class, excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class})
@Import(WorkerEduControllerTest.MockConfig.class)
class WorkerEduControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private WorkerEduService workerEduService;

	@TestConfiguration
	static class MockConfig {
		@Bean
		public WorkerEduService workerEduService() {
			return Mockito.mock(WorkerEduService.class);
		}
	}

	@Test
	@DisplayName("교육이력 등록 성공")
	void recordEduTest() throws Exception {
		// given
		WorkerEduRequest request = new WorkerEduRequest(
			null,
			1L,
			"근로자1",
			"https://cert.com/image.png",
			LocalDate.of(2025, 7, 1)
		);

		WorkerEduResponse response = new WorkerEduResponse(
			1L,
			1L,
			"근로자1",
			"https://cert.com/image.png",
			LocalDate.of(2025, 7, 1)
		);

		when(workerEduService.recordEdu(any())).thenReturn(response);

		// when & then
		mockMvc.perform(post("/worker-education")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.name").value("근로자1"))
			.andExpect(jsonPath("$.data.certUrl").value("https://cert.com/image.png"));
	}

	@Test
	@DisplayName("근로자 교육이력 조회 성공")
	void getEduInfoTest() throws Exception {
		// given
		WorkerEduResponse res1 = new WorkerEduResponse(1L, 1L, "근로자1", "url1", LocalDate.of(2025, 7, 1));
		WorkerEduResponse res2 = new WorkerEduResponse(2L, 1L, "근로자1", "url2", LocalDate.of(2025, 7, 2));
		List<WorkerEduResponse> content = List.of(res1, res2);

		Page<WorkerEduResponse> page = new PageImpl<>(content, PageRequest.of(0, 10), content.size());
		PagedResponse<WorkerEduResponse> pagedResponse = PagedResponse.of(page);

		when(workerEduService.getEduInfo(1L, 0, 10)).thenReturn(pagedResponse);

		// when & then
		mockMvc.perform(get("/worker-education/workers/1?page=0&size=10"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.content.length()").value(2))
			.andExpect(jsonPath("$.data.content[0].name").value("근로자1"));
	}
}