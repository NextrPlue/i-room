package com.iroom.dashboard.dashboard.controller;

import com.iroom.dashboard.dashboard.dto.request.ReportRequest;
import com.iroom.dashboard.dashboard.dto.response.DashBoardResponse;
import com.iroom.dashboard.dashboard.dto.response.MetricResponse;
import com.iroom.dashboard.dashboard.entity.DashBoard;
import com.iroom.dashboard.pdf.service.ChatService;
import com.iroom.dashboard.dashboard.service.DashBoardService;
import com.iroom.dashboard.pdf.service.PdfService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/dashboards")
public class DashBoardController {
	private final DashBoardService dashBoardService;
	private final ChatService chatService;
	private final PdfService pdfService;

	@GetMapping(value = "/metrics/{interval}")
	public  ResponseEntity<List<MetricResponse>> getMetricScore(@PathVariable String interval){//day,week,month
		return ResponseEntity.ok(dashBoardService.getMetricScore(interval));
	}
	//대시보드 조회하기
	@GetMapping(value = "/{metricType}", produces = "application/json;charset=UTF-8")
	public ResponseEntity<DashBoardResponse> getDashBoard(@PathVariable String metricType) {
		DashBoardResponse dashBoardDto = dashBoardService.getDashBoard(metricType);
		return ResponseEntity.ok(dashBoardDto);
	}

	//리포트 생성
	@PostMapping(
		value = "/report/{date}" //yyyy-mm-dd
	)
	public ResponseEntity<byte[]> exportReport(@PathVariable String date) throws Exception {
		List<DashBoard> dashBoards = dashBoardService.getDailyScore(date);
		String missingPpeCnt = "";
		String dangerZoneAccessCnt = "";
		String healthAlertCnt = "";
		for (DashBoard dashBoard : dashBoards){
			switch (dashBoard.getMetricType()){
				case "PPE_VIOLATION" ->missingPpeCnt=String.valueOf(dashBoard.getMetricValue());
				case "DANGER_AREA_ACCESS"->dangerZoneAccessCnt=String.valueOf(dashBoard.getMetricValue());
				case "HEALTH_ANOMALY"->healthAlertCnt=String.valueOf(dashBoard.getMetricValue());
			}
		}
		// 1. 질의 프롬프트 생성
		String userPrompt = String.format(
			"오늘 보호구 미착용 " + missingPpeCnt + "건, 위험지역 접근 " + dangerZoneAccessCnt
				+ "건, 건강 이상 알림 " + healthAlertCnt + "건이 있었습니다. 이에 따른 안전 보고서를 작성해주세요."
		);
		System.out.println("userPrompt: "+userPrompt);

		String context = dashBoardService.getContext(userPrompt);
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
		value = "/improvement-report/{interval}"
	)
	public ResponseEntity<byte[]> createImprovement(@PathVariable String interval) throws Exception {

		String content = dashBoardService.createImprovement(interval);
		LocalDate currentDate = LocalDate.now();
		byte[] pdfBytes = pdfService.generateDashboardPdf(interval+"ly_"+"improvement_report_" + currentDate, content);
		HttpHeaders headers = new HttpHeaders();

		headers.setContentType(MediaType.APPLICATION_PDF);
		headers.setContentDisposition(ContentDisposition.builder("attachment")
			.filename("improvement_report_" + currentDate + ".pdf")
			.build());
		return ResponseEntity.ok().headers(headers).body(pdfBytes);
	}
}