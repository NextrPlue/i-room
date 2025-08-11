package com.iroom.dashboard.service;

import com.iroom.dashboard.dashboard.dto.Message;
import com.iroom.dashboard.pdf.dto.request.ChatRequest;
import com.iroom.dashboard.pdf.dto.response.ChatResponse;
import com.iroom.dashboard.pdf.service.ChatService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

	private ChatService chatService;

	@Mock
	private RestTemplate mockRestTemplate;

	private ChatResponse mockResponse;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		// ChatService 인스턴스 생성
		chatService = new ChatService();

		// Reflection을 사용해 private 필드 주입
		ReflectionTestUtils.setField(chatService, "template", mockRestTemplate);
		ReflectionTestUtils.setField(chatService, "chatModel", "gpt-4");
		ReflectionTestUtils.setField(chatService, "chatApiURL", "http://fake-api");

		// Mock 응답 생성
		Message message = new Message("assistant", "테스트 응답");
		ChatResponse.Choice choice = new ChatResponse.Choice(0, message);
		mockResponse = new ChatResponse(List.of(choice));
	}

	@Test
	@DisplayName("chat() - 성공")
	void chat_Success() {
		// given
		when(mockRestTemplate.postForObject(anyString(), any(ChatRequest.class), eq(ChatResponse.class)))
			.thenReturn(mockResponse);

		// when
		String result = chatService.chat("테스트 프롬프트");

		// then
		assertEquals("테스트 응답", result);
	}

	@Test
	@DisplayName("generateReport() - 성공")
	void generateReport_Success() {
		// given
		when(mockRestTemplate.postForObject(anyString(), any(ChatRequest.class), eq(ChatResponse.class)))
			.thenReturn(mockResponse);

		// when
		String result = chatService.generateReport("테스트 프롬프트");

		// then
		assertEquals("테스트 응답", result);
	}
}
