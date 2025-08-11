package com.iroom.dashboard.pdf.dto.request;

import com.iroom.dashboard.dashboard.dto.Message;

import java.util.List;
import java.util.ArrayList;

public record ChatRequest(
	String model,
	List<Message> messages,
	Double top_p
) {
	public static ChatRequest of(String model, String prompt) {
		List<Message> messages = new ArrayList<>();
		messages.add(new Message("user", prompt));
		return new ChatRequest(model, messages, 0.3);
	}
}
