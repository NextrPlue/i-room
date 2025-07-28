package com.iroom.dashboard.dto.response;

import com.iroom.dashboard.dto.Message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatResponseDto {

	private List<Choice> choices;

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Choice {
		private int index;
		private Message message;
	}
}
