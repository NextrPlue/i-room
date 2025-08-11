package com.iroom.dashboard.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iroom.dashboard.pdf.dto.request.TranslationRequest;
import com.iroom.dashboard.pdf.dto.response.Translation;
import com.iroom.dashboard.pdf.dto.response.TranslationResponse;
import com.iroom.dashboard.pdf.service.ChatService;
import com.iroom.dashboard.pdf.service.TranslationService;
import com.iroom.dashboard.pdf.service.UploadPdfService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UploadPdfServiceTest {

	@InjectMocks
	private UploadPdfService uploadPdfService;

	@Mock
	private TranslationService translationService;

	@Mock
	private ChatService chatService;

	@Mock
	private RestTemplate restTemplate;

	private MockMultipartFile file;

	@BeforeEach
	void setUp() throws IOException {
		InputStream is = getClass().getClassLoader().getResourceAsStream("sample.pdf");
		file = new MockMultipartFile("file", "sample.pdf", "application/pdf", is);
	}

	@Test
	@DisplayName("uploadReport - 성공")
	void uploadReport_Success() throws JsonProcessingException {
		// given
		TranslationResponse translationResponse = new TranslationResponse(List.of(new Translation("EN", "test data")));
		when(translationService.translate(any(TranslationRequest.class))).thenReturn(translationResponse);
		when(chatService.question(anyString())).thenReturn("summary text");

		String jsonResponse = """
			{"result":{"operation_id":2,"status":"completed"},"status":"ok","time":0.0110805}
			""";
		when(restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(), eq(String.class)))
			.thenReturn(ResponseEntity.ok(jsonResponse));

		// when
		ResponseEntity<String> result = uploadPdfService.uploadReport(file);
		String responseBody = result.getBody();
		// then

		// JSON 파싱
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode root = objectMapper.readTree(responseBody);

		// 최상위 "status" 접근
		String status = root.get("status").asText();

		assertEquals("ok", status);
	}

}
