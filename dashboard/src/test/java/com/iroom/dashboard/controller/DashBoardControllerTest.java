package com.iroom.dashboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iroom.dashboard.dto.request.ReportRequest;
import com.iroom.dashboard.dto.response.DashBoardResponse;
import com.iroom.dashboard.service.ChatService;
import com.iroom.dashboard.service.DashBoardService;
import com.iroom.dashboard.service.EmbeddingService;
import com.iroom.dashboard.service.PdfService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = DashBoardController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@Import(DashBoardControllerTest.MockConfig.class)
class DashBoardControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private DashBoardService dashBoardService;

	@Autowired
	private ChatService chatService;

	@Autowired
	private PdfService pdfService;

	@Autowired
	private EmbeddingService embeddingService;

	@Autowired
	private RestTemplate restTemplate;

	@TestConfiguration
	static class MockConfig {
		@Bean
		public DashBoardService dashBoardService() {
			return Mockito.mock(DashBoardService.class);
		}

		@Bean
		public ChatService chatService() {
			return Mockito.mock(ChatService.class);
		}

		@Bean
		public PdfService pdfService() {
			return Mockito.mock(PdfService.class);
		}

		@Bean
		public EmbeddingService embeddingService() {
			return Mockito.mock(EmbeddingService.class);
		}

		@Bean
		public RestTemplate restTemplate() {
			return Mockito.mock(RestTemplate.class);
		}
	}

	@Test
	@DisplayName("GET /dashboards/{metricType} - 대시보드 조회 성공")
	void getDashBoard_Success() throws Exception {
		// given
		DashBoardResponse dashBoardResponse = new DashBoardResponse("t", 100, LocalDateTime.now());
		when(dashBoardService.getDashBoard("t")).thenReturn(dashBoardResponse);

		// when & then
		mockMvc.perform(get("/dashboards/t")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.metricType").value("t"))
			.andExpect(jsonPath("$.metricValue").value(100))
			.andExpect(jsonPath("$.recordedAt").exists());
	}

	@Test
	@DisplayName("POST /dashboards/report - 리포트 생성 성공")
	void exportReport_Success() throws Exception {
		// given
		ReportRequest request = new ReportRequest(1, 2, 3);
		float[] dummyVector = new float[1536];
		for (int i = 0; i < 1536; i++) {
			dummyVector[i] = 0.0f;
		}
		byte[] pdfBytes = "PDF CONTENT".getBytes();

		when(embeddingService.embed(anyString())).thenReturn(dummyVector);

		Map<String, Object> payload = Map.of("content", "위험 내용입니다.");
		Map<String, Object> item = Map.of("payload", payload);
		Map<String, Object> qdrantResponse = Map.of("result", List.of(item));

		when(restTemplate.postForEntity(anyString(), any(), Mockito.eq(Map.class)))
			.thenReturn(ResponseEntity.ok(qdrantResponse));

		when(chatService.questionReport(anyString())).thenReturn("GPT 응답입니다.");
		when(pdfService.generateDashboardPdf(anyString(), anyString())).thenReturn(pdfBytes);

		// when & then
		mockMvc.perform(post("/dashboards/report")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_PDF))
			.andExpect(header().string("Content-Disposition",
				"attachment; filename=\"report_" + LocalDate.now() + ".pdf\""))
			.andExpect(content().bytes(pdfBytes));
	}

	@Test
	@DisplayName("POST /dashboards/improvement-report - 개선안 생성 성공")
	void createImprovement_Success() throws Exception {
		// given
		byte[] pdfBytes = "개선안 PDF".getBytes();
		when(pdfService.generateDashboardPdf(anyString(), anyString())).thenReturn(pdfBytes);

		// when & then
		mockMvc.perform(post("/dashboards/improvement-report"))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_PDF))
			.andExpect(header().string("Content-Disposition",
				"attachment; filename=\"improvement_report_" + LocalDate.now() + ".pdf\""))
			.andExpect(content().bytes(pdfBytes));
	}
}