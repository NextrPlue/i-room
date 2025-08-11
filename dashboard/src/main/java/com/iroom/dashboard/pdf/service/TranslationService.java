package com.iroom.dashboard.pdf.service;

import com.iroom.dashboard.pdf.dto.request.TranslationRequest;
import com.iroom.dashboard.pdf.dto.response.TranslationResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Service
public class TranslationService {
	private final String deeplApiUrl = "https://api-free.deepl.com/v2/translate";
	@Value("${myapp.auth.key}")
	private String authKey;

	public TranslationResponse translate(TranslationRequest request) {
		String targetLang = request.target_lang();

		String apiUrl = deeplApiUrl + "?target_lang=" + targetLang;
		RestTemplate restTemplate = new RestTemplate();

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Authorization", "DeepL-Auth-Key " + authKey);

		TranslationResponse response = restTemplate.postForObject(apiUrl, createHttpEntity(request, headers),
			TranslationResponse.class);

		return response;
	}

	private HttpEntity<TranslationRequest> createHttpEntity(TranslationRequest request, HttpHeaders headers) {
		return new HttpEntity<>(request, headers);
	}
}
