package com.iroom.dashboard.service;

import com.iroom.dashboard.dto.request.TranslationRequest;
import com.iroom.dashboard.dto.response.Translation;
import com.iroom.dashboard.dto.response.TranslationResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
class UploadPdfServiceTest {

	@Autowired
	private UploadPdfService uploadPdfService;

	@Mock
	private RestTemplate restTemplate;

	@Mock
	private TranslationService translationService;

	@Mock
	private ChatService chatService;

	private MockMultipartFile file;

	@BeforeEach
	void setUp() throws IOException {
		file = new MockMultipartFile("file", "test.pdf", "application/pdf", "test data".getBytes());
	}

	@Test
	@DisplayName("uploadReport - 성공")
	void uploadReport_Success() {
		// given
		TranslationResponse translationResponse = new TranslationResponse(List.of(new Translation("EN", "test data")));
		when(translationService.translate(any(TranslationRequest.class))).thenReturn(translationResponse);
		when(chatService.question(anyString())).thenReturn("summary text");
		when(restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(), eq(String.class)))
			.thenReturn(ResponseEntity.ok("success"));

		// when
		ResponseEntity<String> result = uploadPdfService.uploadReport(file);

		// then
		assertEquals("success", result.getBody());
	}
}