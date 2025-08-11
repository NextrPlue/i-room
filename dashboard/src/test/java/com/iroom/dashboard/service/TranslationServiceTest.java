package com.iroom.dashboard.service;

import com.iroom.dashboard.pdf.dto.request.TranslationRequest;
import com.iroom.dashboard.pdf.dto.response.Translation;
import com.iroom.dashboard.pdf.dto.response.TranslationResponse;
import com.iroom.dashboard.pdf.service.TranslationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
class TranslationServiceTest {

	@Autowired
	private TranslationService translationService;

	@Mock
	private RestTemplate restTemplate;

	private TranslationResponse translationResponse;

	@BeforeEach
	void setUp() {
		Translation translation = new Translation("KO", "안녕하세요");
		translationResponse = new TranslationResponse(List.of(translation));
	}

	@Test
	@DisplayName("translate - 성공")
	void translate_Success() {
		// given
		TranslationRequest request = new TranslationRequest(List.of("Hello"), "KO");
		when(restTemplate.postForObject(anyString(), any(), eq(TranslationResponse.class)))
			.thenReturn(translationResponse);

		// when
		TranslationResponse result = translationService.translate(request);

		// then
		assertEquals("안녕하세요", result.translations().get(0).getText());
	}
}