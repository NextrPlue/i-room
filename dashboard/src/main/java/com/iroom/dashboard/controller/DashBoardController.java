package com.iroom.dashboard.controller;

import com.iroom.dashboard.dto.request.ReportRequest;
import com.iroom.dashboard.dto.response.DashBoardResponse;
import com.iroom.dashboard.service.ChatService;
import com.iroom.dashboard.service.DashBoardService;
import com.iroom.dashboard.service.EmbeddingService;
import com.iroom.dashboard.service.PdfService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/dashboards")
public class DashBoardController {
	private final DashBoardService dashBoardService;
	private final ChatService chatService;
	private final PdfService pdfService;


	//대시보드 조회하기
	@GetMapping(value = "/{metricType}", produces = "application/json;charset=UTF-8")
	public ResponseEntity<DashBoardResponse> getDashBoard(@PathVariable String metricType) {
		DashBoardResponse dashBoardDto = dashBoardService.getDashBoard(metricType);
		return ResponseEntity.ok(dashBoardDto);
	}

	//리포트 생성
	@PostMapping(
		value = "/report"
	)
	public ResponseEntity<byte[]> exportReport(@RequestBody ReportRequest reportRequest) throws Exception {

		// 1. 질의 프롬프트 생성
		String userPrompt = String.format(
			"오늘 보호구 미착용 " + reportRequest.missingPpeCnt() + "건, 위험지역 접근 " + reportRequest.dangerZoneAccessCnt()
				+ "건, 건강 이상 알림 " + reportRequest.healthAlertCnt() + "건이 있었습니다. 이에 따른 안전 보고서를 작성해주세요."
		);


		String context = dashBoardService.getContext(reportRequest,userPrompt);
		String finalPrompt = context + "\n" + userPrompt;

		// 4. GPT에 최종 질의
		String gptResponse = chatService.questionReport(finalPrompt);
		// 5. 리포트 PDF로 생성
		LocalDate currentDate = LocalDate.now();
		byte[] pdfBytes = pdfService.generateDashboardPdf("report_" + currentDate, gptResponse);
		HttpHeaders pdfHeaders = new HttpHeaders();
		pdfHeaders.setContentType(MediaType.APPLICATION_PDF);
		pdfHeaders.setContentDisposition(ContentDisposition.builder("attachment")
			.filename("report_" + currentDate + ".pdf") //
			.build());
		return ResponseEntity.ok().headers(pdfHeaders).body(pdfBytes);
	}

	//개선안 생성
	@PostMapping(
		value = "/improvement-report"
	)
	public ResponseEntity<byte[]> createImprovement(
	) throws Exception {
		LocalDate currentDate = LocalDate.now();
		String content = "";
		byte[] pdfBytes = pdfService.generateDashboardPdf("improvement_report_" + currentDate, content);
		HttpHeaders headers = new HttpHeaders();

		headers.setContentType(MediaType.APPLICATION_PDF);
		headers.setContentDisposition(ContentDisposition.builder("attachment")
			.filename("improvement_report_" + currentDate + ".pdf")
			.build());
		return ResponseEntity.ok().headers(headers).body(pdfBytes);
	}
}