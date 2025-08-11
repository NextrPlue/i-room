package com.iroom.dashboard.pdf.service;

import com.iroom.dashboard.pdf.dto.request.TranslationRequest;
import com.iroom.dashboard.pdf.dto.response.Translation;
import com.iroom.dashboard.pdf.dto.response.TranslationResponse;

import lombok.RequiredArgsConstructor;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UploadPdfService {

	private final RestTemplate restTemplate;
	private final String QDRANT_URL = "http://localhost:6333/collections/safety_db/points?wait=true";
	private final TranslationService translationService;
	private final ChatService chatService;

	public ResponseEntity<String> uploadReport(@RequestParam("file") MultipartFile file) {
		try {
			// 1. PDF에서 텍스트 추출
			String context = extractTextFromPDF(file);

			// 2. 3000자 단위로 영어로 변환 요청
			int chunkSize = 3000;
			int start = 0;
			String translatedContext = ""; //번역된 영문
			while (start < context.length()) {
				int end = Math.min(start + chunkSize, context.length());
				String chunk = context.substring(start, end);
				List<String> promptList = new ArrayList<>();
				promptList.add(chunk);
				// TranslationRequest translationRequest = TranslationRequest.builder().
				// 	text(promptList).target_lang("EN").
				// 	build();
				TranslationRequest translationRequest = new TranslationRequest(promptList, "EN");

				TranslationResponse translated = translationService.translate(translationRequest);
				for (Translation t : translated.translations()) {
					translatedContext += t.getText();
				}
				start = end;
			}
			String summaryText = chatService.question(translatedContext);

			// 3. 벡터: 임의 벡터 (예: 5차원 0값)
			float[] dummyVector = new float[1536];
			for (int i = 0; i < 1536; i++) {
				dummyVector[i] = 0.0f;
			}
			// 4. Qdrant 요청 payload 구성
			Map<String, Object> payload = new HashMap<>();
			List<Map<String, Object>> points = new ArrayList<>();

			Map<String, Object> point = new HashMap<>();
			point.put("id", UUID.randomUUID().toString());
			point.put("vector", dummyVector);
			point.put("payload", Map.of("content", summaryText));

			points.add(point);
			payload.put("points", points);

			// 5. Qdrant에 저장 요청
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

			ResponseEntity<String> response = restTemplate.exchange(
				QDRANT_URL,
				HttpMethod.PUT,
				request,
				String.class
			);

			return response;

		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("PDF 처리 오류: " + e.getMessage());
		}
	}

	private String extractTextFromPDF(MultipartFile file) throws IOException {
		try (PDDocument document = PDDocument.load(file.getInputStream())) {
			return new PDFTextStripper().getText(document);
		}
	}

}
