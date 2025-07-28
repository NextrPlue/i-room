package com.iroom.dashboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iroom.dashboard.dto.request.BlueprintRequest;
import com.iroom.dashboard.dto.response.BlueprintResponse;
import com.iroom.dashboard.dto.response.PagedResponse;
import com.iroom.dashboard.service.BlueprintService;

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

@WebMvcTest(BlueprintController.class)
@Import(BlueprintControllerTest.MockConfig.class)
class BlueprintControllerTest {

	@Autowired private MockMvc mockMvc;
	@Autowired private ObjectMapper objectMapper;
	@Autowired private BlueprintService blueprintService; // 직접 주입받음

	@TestConfiguration
	static class MockConfig {
		@Bean
		public BlueprintService blueprintService() {
			return Mockito.mock(BlueprintService.class);
		}
	}

	@Test
	@DisplayName("도면 등록 성공")
	void createBlueprintTest() throws Exception {
		BlueprintRequest request = new BlueprintRequest("url.png", 1, 100.0, 200.0);
		BlueprintResponse response = new BlueprintResponse(1L, "url.png", 1, 100.0, 200.0);

		Mockito.when(blueprintService.createBlueprint(any())).thenReturn(response);

		mockMvc.perform(post("/blueprints")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.blueprintUrl").value("url.png"))
			.andExpect(jsonPath("$.floor").value(1));
	}

	@Test
	@DisplayName("도면 수정 성공")
	void updateBlueprintTest() throws Exception {
		BlueprintRequest request = new BlueprintRequest("new_url.png", 2, 150.0, 250.0);
		BlueprintResponse response = new BlueprintResponse(1L, "new_url.png", 2, 150.0, 250.0);

		Mockito.when(blueprintService.updateBlueprint(eq(1L), any())).thenReturn(response);

		mockMvc.perform(put("/blueprints/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.blueprintUrl").value("new_url.png"))
			.andExpect(jsonPath("$.floor").value(2));
	}

	@Test
	@DisplayName("도면 삭제 성공")
	void deleteBlueprintTest() throws Exception {
		Mockito.doNothing().when(blueprintService).deleteBlueprint(1L);

		mockMvc.perform(delete("/blueprints/1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("도면 삭제 완료"))
			.andExpect(jsonPath("$.deletedId").value(1));
	}

	@Test
	@DisplayName("도면 전체 조회 성공")
	void getAllBlueprintsTest() throws Exception {
		BlueprintResponse r1 = new BlueprintResponse(1L, "url1.png", 1, 100.0, 100.0);
		BlueprintResponse r2 = new BlueprintResponse(2L, "url2.png", 2, 200.0, 200.0);

		List<BlueprintResponse> content = List.of(r1, r2);
		Page<BlueprintResponse> page = new PageImpl<>(content, PageRequest.of(0, 10), content.size());
		PagedResponse<BlueprintResponse> pagedResponse = PagedResponse.of(page);

		Mockito.when(blueprintService.getAllBlueprints(0, 10)).thenReturn(pagedResponse);

		mockMvc.perform(get("/blueprints?page=0&size=10"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content").isArray())
			.andExpect(jsonPath("$.content.length()").value(2))
			.andExpect(jsonPath("$.content[0].floor").value(1));
	}
}