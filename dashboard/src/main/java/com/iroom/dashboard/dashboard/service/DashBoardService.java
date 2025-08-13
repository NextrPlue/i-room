package com.iroom.dashboard.dashboard.service;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.iroom.dashboard.dashboard.dto.request.ReportRequest;
import com.iroom.dashboard.dashboard.dto.response.DashBoardResponse;
import com.iroom.dashboard.dashboard.dto.response.MetricResponse;
import com.iroom.dashboard.dashboard.entity.DashBoard;
import com.iroom.dashboard.dashboard.repository.DashBoardRepository;
import com.iroom.dashboard.pdf.service.ChatService;
import com.iroom.dashboard.pdf.service.EmbeddingService;
import com.iroom.dashboard.pdf.service.PdfService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Transactional
@Service
@RequiredArgsConstructor
public class DashBoardService {
	private final RestTemplate restTemplate = new RestTemplate();
	private final EmbeddingService embeddingService;
	private final DashBoardRepository dashBoardRepository;
	private final ChatService chatService;
	@Value("${qdrant.url}")
	private String qdrantUrl;

	@PreAuthorize("hasAnyAuthority('ROLE_WORKER', 'ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_READER', 'ROLE_WORKER_SYSTEM')")
	public List<MetricResponse> getMetricScore(String interval) {
		List<Object[]> rows = switch (interval.toLowerCase()) {
			case "day" -> dashBoardRepository.getDailyMetricSummaryRaw();
			case "week" -> dashBoardRepository.getWeeklyMetricSummaryRaw();
			case "month" -> dashBoardRepository.getMonthlyMetricSummaryRaw();
			default -> throw new IllegalArgumentException("Invalid interval: " + interval);
		};
		return rows.stream()
			.map(row -> new MetricResponse(
				((Date)row[0]).toLocalDate(), // 날짜
				(String)row[1], // 사고 유형
				((Number)row[2]).intValue() //누적값
			))
			.toList();
	}

	@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_READER')")
	public DashBoardResponse getDashBoard(String metricType) {
		DashBoard dashBoard = dashBoardRepository.findTopByMetricTypeOrderByIdDesc(metricType);

		DashBoardResponse dashBoardResponse = new DashBoardResponse(
			dashBoard.getMetricType(),
			dashBoard.getMetricValue(),
			dashBoard.getRecordedAt()
		);

		return dashBoardResponse;
	}
	//벡터 디비에 질의
	public String getContext(String userPrompt) {
		// 2. Qdrant에 유사 문단 질의 (임의 벡터 사용 중이라 실제 의미 없음)
		//float[] dummyVector = new float[]{0f, 0f, 0f, 0f, 0f};
		float[] embeddedVector = embeddingService.embed(userPrompt);

		Map<String, Object> qdrantRequest = new HashMap<>();
		//qdrantRequest.put("vector", dummyVector);
		qdrantRequest.put("vector", embeddedVector);
		qdrantRequest.put("limit", 5);
		qdrantRequest.put("with_payload", true);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<Map<String, Object>> request = new HttpEntity<>(qdrantRequest, headers);

		ResponseEntity<Map> qdrantResponse = restTemplate.postForEntity(
			qdrantUrl + "/collections/safety_db/points/search",
			request,
			Map.class
		);

		// 3. Qdrant 응답에서 관련 텍스트 추출
		StringBuilder contextBuilder = new StringBuilder();
		try {
			var resultList = (Iterable<Map<String, Object>>)qdrantResponse.getBody().get("result");
			for (Map<String, Object> item : resultList) {
				Map<String, Object> payload = (Map<String, Object>)item.get("payload");
				if (payload != null && payload.containsKey("content")) {
					contextBuilder.append(payload.get("content")).append("\n\n");
				}
			}
		} catch (Exception e) {
			return "Qdrant 검색 중 오류가 발생했습니다.";

		}
		return contextBuilder.toString();
	}
	public  String createImprovement(String interval){
		String userPrompt = interval+ "별 종합 안전점수 기록\n";
		List<Object[]> rows = switch (interval.toLowerCase()) {
			case "day" -> dashBoardRepository.getDailyMetricSummaryRaw();
			case "week" -> dashBoardRepository.getWeeklyMetricSummaryRaw();
			case "month" -> dashBoardRepository.getMonthlyMetricSummaryRaw();
			default -> throw new IllegalArgumentException("Invalid interval: " + interval);
		};
		int idx = 1;
		for (Object[] row: rows){
			userPrompt+=(idx+"주 안전점수 기록\n");
			userPrompt+=("날짜: "+ row[0]+"\n");
			userPrompt+=("사고유형: "+ row[1]+"\n");
			userPrompt+=("발생횟수: "+ row[2]+"\n");
			idx+=1;
			if(idx >30){
				break;
			}
		}
		System.out.println(userPrompt);
		String finalPrompt = getContext(userPrompt)+userPrompt;
		String gptResponse = chatService.questionReport(finalPrompt);

		return gptResponse;
	}
}
