package com.iroom.dashboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iroom.dashboard.dashboard.controller.DashBoardController;
import com.iroom.dashboard.dashboard.dto.request.ReportRequest;
import com.iroom.dashboard.dashboard.entity.DashBoard;
import com.iroom.dashboard.report.entity.Report;
import com.iroom.dashboard.dashboard.dto.response.DashBoardResponse;
import com.iroom.dashboard.pdf.service.ChatService;
import com.iroom.dashboard.dashboard.service.DashBoardService;
import com.iroom.dashboard.pdf.service.EmbeddingService;
import com.iroom.dashboard.pdf.service.PdfService;
import com.iroom.dashboard.report.service.ReportService;

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

	@Autowired
	private ReportService reportService;

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

		@Bean
		public ReportService reportService() {
			return Mockito.mock(ReportService.class);
		}
	}

	@Test
	@DisplayName("GET /dashboards/{metricType} - 대시보드 조회 성공")
	void getDashBoard_Success() throws Exception {
		// given
		DashBoardResponse dashBoardResponse = new DashBoardResponse("t", 100, LocalDate.now());
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
	@DisplayName("POST /dashboards/report/{date} - 리포트 생성 성공")
	void exportReport_Success() throws Exception {
		// given
		String testDate = "2024-01-15";
		List<DashBoard> dashBoards = List.of(
			DashBoard.builder()
				.metricType("PPE_VIOLATION")
				.metricValue(5)
				.build(),
			DashBoard.builder()
				.metricType("DANGER_ZONE")
				.metricValue(3)
				.build(),
			DashBoard.builder()
				.metricType("HEALTH_RISK")
				.metricValue(2)
				.build()
		);
		
		float[] dummyVector = new float[1536];
		for (int i = 0; i < 1536; i++) {
			dummyVector[i] = 0.0f;
		}
		byte[] pdfBytes = "PDF CONTENT".getBytes();

		when(dashBoardService.getDailyScore(testDate)).thenReturn(dashBoards);
		when(dashBoardService.getContext(anyString())).thenReturn("context");
		when(embeddingService.embed(anyString())).thenReturn(dummyVector);

		Map<String, Object> payload = Map.of("content", "위험 내용입니다.");
		Map<String, Object> item = Map.of("payload", payload);
		Map<String, Object> qdrantResponse = Map.of("result", List.of(item));

		when(restTemplate.postForEntity(anyString(), any(), Mockito.eq(Map.class)))
			.thenReturn(ResponseEntity.ok(qdrantResponse));

		when(chatService.questionReport(anyString())).thenReturn("GPT 응답입니다.");
		when(pdfService.generateDashboardPdf(anyString(), anyString())).thenReturn(pdfBytes);
		
		Report mockReport = Report.builder()
			.reportName("daily_safety_report_" + testDate)
			.reportType(Report.ReportType.DAILY_REPORT)
			.reportUrl("/uploads/reports/test.pdf")
			.period(testDate)
			.build();
		when(reportService.saveReport(any(), anyString(), any(), anyString())).thenReturn(mockReport);

		// when & then
		mockMvc.perform(post("/dashboards/report/" + testDate))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_PDF))
			.andExpect(header().string("Content-Disposition",
				"attachment; filename=\"report_" + LocalDate.now() + ".pdf\""))
			.andExpect(content().bytes(pdfBytes));
	}

	@Test
	@DisplayName("POST /dashboards/improvement-report/{interval} - 개선안 생성 성공")
	void createImprovement_Success() throws Exception {
		// given
		String interval = "week";
		String testPeriod = "2024-01-01 ~ 2024-01-31";
		byte[] pdfBytes = "개선안 PDF".getBytes();
		
		when(dashBoardService.createImprovement(interval)).thenReturn("개선안 내용");
		when(dashBoardService.getImprovementPeriod(interval)).thenReturn(testPeriod);
		when(pdfService.generateDashboardPdf(anyString(), anyString())).thenReturn(pdfBytes);
		
		Report mockReport = Report.builder()
			.reportName(interval + "ly_improvement_report_" + LocalDate.now())
			.reportType(Report.ReportType.IMPROVEMENT_REPORT)
			.reportUrl("/uploads/reports/test.pdf")
			.period(testPeriod)
			.build();
		when(reportService.saveReport(any(), anyString(), any(), anyString())).thenReturn(mockReport);

		// when & then
		mockMvc.perform(post("/dashboards/improvement-report/" + interval))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_PDF))
			.andExpect(header().string("Content-Disposition",
				"attachment; filename=\"improvement_report_" + LocalDate.now() + ".pdf\""))
			.andExpect(content().bytes(pdfBytes));
	}
}