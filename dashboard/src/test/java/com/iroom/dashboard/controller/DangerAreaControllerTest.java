package com.iroom.dashboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iroom.dashboard.dto.request.DangerAreaRequest;
import com.iroom.dashboard.dto.response.DangerAreaResponse;
import com.iroom.dashboard.service.DangerAreaService;
import com.iroom.modulecommon.dto.response.PagedResponse;

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

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DangerAreaController.class)
@Import(DangerAreaControllerTest.MockConfig.class)
class DangerAreaControllerTest {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private DangerAreaService dangerAreaService;

	@TestConfiguration
	static class MockConfig {
		@Bean
		public DangerAreaService dangerAreaService() {
			return Mockito.mock(DangerAreaService.class);
		}
	}

	@Test
	@DisplayName("위험구역 등록 성공")
	void createDangerAreaTest() throws Exception {
		// given
		DangerAreaRequest request = new DangerAreaRequest(1L, "X:10, Y:20", 100.0, 200.0);
		DangerAreaResponse response = new DangerAreaResponse(1L, 1L, "X:10, Y:20", 100.0, 200.0);

		Mockito.when(dangerAreaService.createDangerArea(any())).thenReturn(response);

		// when & then
		mockMvc.perform(post("/danger-areas")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.location").value("X:10, Y:20"))
			.andExpect(jsonPath("$.blueprintId").value(1));
	}

	@Test
	@DisplayName("위험구역 수정 성공")
	void updateDangerAreaTest() throws Exception {
		// given
		DangerAreaRequest request = new DangerAreaRequest(1L, "X:99, Y:88", 120.0, 220.0);
		DangerAreaResponse response = new DangerAreaResponse(1L, 1L, "X:99, Y:88", 120.0, 220.0);

		Mockito.when(dangerAreaService.updateDangerArea(eq(1L), any())).thenReturn(response);

		// when & then
		mockMvc.perform(put("/danger-areas/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.location").value("X:99, Y:88"))
			.andExpect(jsonPath("$.width").value(120.0));
	}

	@Test
	@DisplayName("위험구역 삭제 성공")
	void deleteDangerAreaTest() throws Exception {
		// given
		Mockito.doNothing().when(dangerAreaService).deleteDangerArea(1L);

		// when & then
		mockMvc.perform(delete("/danger-areas/1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("위험구역 삭제 완료"))
			.andExpect(jsonPath("$.deletedId").value(1));
	}

	@Test
	@DisplayName("위험구역 전체 조회 성공")
	void getAllDangerAreasTest() throws Exception {
		// given
		DangerAreaResponse r1 = new DangerAreaResponse(1L, 1L, "X:10, Y:20", 100.0, 200.0);
		DangerAreaResponse r2 = new DangerAreaResponse(2L, 1L, "X:30, Y:40", 150.0, 250.0);

		List<DangerAreaResponse> content = List.of(r1, r2);
		Page<DangerAreaResponse> page = new PageImpl<>(content, PageRequest.of(0, 10), content.size());
		PagedResponse<DangerAreaResponse> pagedResponse = PagedResponse.of(page);

		Mockito.when(dangerAreaService.getAllDangerAreas(0, 10)).thenReturn(pagedResponse);

		// when & then
		mockMvc.perform(get("/danger-areas?page=0&size=10"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content").isArray())
			.andExpect(jsonPath("$.content.length()").value(2))
			.andExpect(jsonPath("$.content[0].location").value("X:10, Y:20"));
	}
}