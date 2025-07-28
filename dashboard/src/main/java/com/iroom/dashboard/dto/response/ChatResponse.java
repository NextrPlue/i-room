package com.iroom.dashboard.dto.response;

import com.iroom.dashboard.dto.Message;

import java.util.List;

public record ChatResponse(
	List<Choice> choices
) {
	public record Choice(
		int index,
		Message message
	) {
	}
}
