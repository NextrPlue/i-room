package com.iroom.dashboard.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
public class EmbeddingServiceImpl implements EmbeddingService {

	private final RestTemplate restTemplate = new RestTemplate();
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Value("${openai.api.key}")
	private String openAiApiKey;

	private static final String EMBEDDING_URL = "https://api.openai.com/v1/embeddings";

	@Override
	public float[] embed(String text) {
		try {
			// JSON 요청 구성
			Map<String, Object> body = new HashMap<>();
			body.put("input", text);
			body.put("model", "text-embedding-3-small"); // 최신 모델 (2024 기준)

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setBearerAuth(openAiApiKey);

			HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

			ResponseEntity<String> response = restTemplate.postForEntity(
				EMBEDDING_URL,
				request,
				String.class
			);

			// 응답 파싱
			JsonNode root = objectMapper.readTree(response.getBody());
			JsonNode embeddingNode = root.path("data").get(0).path("embedding");

			float[] vector = new float[embeddingNode.size()];
			for (int i = 0; i < embeddingNode.size(); i++) {
				vector[i] = (float)embeddingNode.get(i).asDouble();
			}

			return vector;

		} catch (Exception e) {
			throw new RuntimeException("임베딩 실패: " + e.getMessage(), e);
		}
	}
}
