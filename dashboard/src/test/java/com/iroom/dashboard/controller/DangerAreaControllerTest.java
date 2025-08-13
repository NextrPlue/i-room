package com.iroom.dashboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iroom.dashboard.danger.controller.DangerAreaController;
import com.iroom.dashboard.danger.dto.request.DangerAreaRequest;
import com.iroom.dashboard.danger.dto.response.DangerAreaResponse;
import com.iroom.dashboard.danger.service.DangerAreaService;
import com.iroom.modulecommon.dto.response.PagedResponse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
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

@WebMvcTest(controllers = DangerAreaController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
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
		DangerAreaRequest request = new DangerAreaRequest(1L, 10.0, 20.0, 100.0, 200.0, "테스트 위험구역");
		DangerAreaResponse response = new DangerAreaResponse(1L, 1L, 10.0, 20.0, 100.0, 200.0, "테스트 위험구역");

		Mockito.when(dangerAreaService.createDangerArea(any())).thenReturn(response);

		// when & then
		mockMvc.perform(post("/danger-areas")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.latitude").value(10.0))
			.andExpect(jsonPath("$.data.longitude").value(20.0))
			.andExpect(jsonPath("$.data.blueprintId").value(1))
			.andExpect(jsonPath("$.data.name").value("테스트 위험구역"));
	}

	@Test
	@DisplayName("위험구역 수정 성공")
	void updateDangerAreaTest() throws Exception {
		// given
		DangerAreaRequest request = new DangerAreaRequest(1L, 99.0, 88.0, 120.0, 220.0, "수정된 위험구역");
		DangerAreaResponse response = new DangerAreaResponse(1L, 1L, 99.0, 88.0, 120.0, 220.0, "수정된 위험구역");

		Mockito.when(dangerAreaService.updateDangerArea(eq(1L), any())).thenReturn(response);

		// when & then
		mockMvc.perform(put("/danger-areas/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.latitude").value(99.0))
			.andExpect(jsonPath("$.data.longitude").value(88.0))
			.andExpect(jsonPath("$.data.width").value(120.0))
			.andExpect(jsonPath("$.data.name").value("수정된 위험구역"));
	}

	@Test
	@DisplayName("위험구역 삭제 성공")
	void deleteDangerAreaTest() throws Exception {
		// given
		Mockito.doNothing().when(dangerAreaService).deleteDangerArea(1L);

		// when & then
		mockMvc.perform(delete("/danger-areas/1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.message").value("위험구역 삭제 완료"))
			.andExpect(jsonPath("$.data.deletedId").value(1));
	}

	@Test
	@DisplayName("위험구역 전체 조회 성공")
	void getAllDangerAreasTest() throws Exception {
		// given
		DangerAreaResponse r1 = new DangerAreaResponse(1L, 1L, 10.0, 20.0, 100.0, 200.0, "위험구역1");
		DangerAreaResponse r2 = new DangerAreaResponse(2L, 1L, 30.0, 40.0, 150.0, 250.0, "위험구역2");

		List<DangerAreaResponse> content = List.of(r1, r2);
		Page<DangerAreaResponse> page = new PageImpl<>(content, PageRequest.of(0, 10), content.size());
		PagedResponse<DangerAreaResponse> pagedResponse = PagedResponse.of(page);

		Mockito.when(dangerAreaService.getAllDangerAreas(0, 10)).thenReturn(pagedResponse);

		// when & then
		mockMvc.perform(get("/danger-areas?page=0&size=10"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.content").isArray())
			.andExpect(jsonPath("$.data.content.length()").value(2))
			.andExpect(jsonPath("$.data.content[0].latitude").value(10.0))
			.andExpect(jsonPath("$.data.content[0].longitude").value(20.0))
			.andExpect(jsonPath("$.data.content[0].name").value("위험구역1"));
	}
}