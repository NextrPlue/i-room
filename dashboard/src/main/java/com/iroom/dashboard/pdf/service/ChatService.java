package com.iroom.dashboard.pdf.service;

import com.iroom.dashboard.dashboard.dto.Message;
import com.iroom.dashboard.pdf.dto.request.ChatRequest;
import com.iroom.dashboard.pdf.dto.response.ChatResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class ChatService {
	@Value("${openai.model.chat}")
	private String chatModel;

	@Value("${openai.api.url.chat}")
	private String chatApiURL;

	@Autowired
	private RestTemplate template;

	public String chat(String prompt) {
		ChatRequest request = ChatRequest.of(chatModel, prompt);
		request.messages().add(new Message(
			"system",
			"General Purpose) Construction Safety Document Summarization Prompt for LLMs\n" +
				"1. Overview\n" +
				"\n" +
				"You are an AI assistant designed to analyze uploaded technical documents related to construction site safety and summarize their key contents.\n"
				+
				"The goal of the final summary is to help site managers, safety officers, or workers quickly and clearly understand the critical risk factors and essential preventive measures, without reading the entire document.\n"
				+
				"\n" +
				"2. Output Format\n" +
				"\n" +
				"The summary should follow the structured format below. Fill in each section based on the document's content and structure.\n"
				+
				"\n" +
				"A. Basic Document Information\n" +
				"\n" +
				"Document Title:\n" +
				"\n" +
				"Publisher/Source:\n" +
				"\n" +
				"Publication Date (or Version):\n" +
				"\n" +
				"Core Purpose: (e.g., Preventing falls at construction sites, Fire and explosion response manual, etc.)\n"
				+
				"\n" +
				"B. Key Content Summary\n" +
				"\n" +
				"Review the document’s table of contents or main structure to identify major sections or categories.\n"
				+
				"Summarize each by grouping relevant content under clear headings.\n" +
				"\n" +
				"For each section (or individual risk factor), extract and list the following items:\n" +
				"\n" +
				"Section Title or Risk Factor Name: (e.g., Fall Hazard: Roofing Work)\n" +
				"\n" +
				"Key Statistics (if available): Provide brief statistical data if mentioned (e.g., 170 fatalities over the past 5 years).\n"
				+
				"\n" +
				"Essential Safety Rules and Preventive Measures: Summarize the most important safety protocols or countermeasures in 2–4 bullet points (-).\n"
				+
				"\n" +
				"Mandatory Worker Guidelines (if applicable): If there are instructions specifically directed at workers (e.g., “Workers must always…”), include them here.\n"
				+
				"\n" +
				"3. Additional Instructions and Considerations\n" +
				"\n" +
				"Indicate Intended Audience: Mention who the summary is most useful for (e.g., site managers, safety supervisors, general workers) at the beginning of the output.\n"
				+
				"\n" +
				"Flexibility: This is a standard template. If the source document follows a different structure (e.g., cause-effect, legal regulation–explanation), adapt the summary format accordingly to best reflect the content and its purpose."));
		try {
			ChatResponse response = template.postForObject(chatApiURL, request, ChatResponse.class);
			return response.choices().get(0).message().getContent();

		} catch (Exception e) {
			System.out.println("gpt에러: " + e);
		}
		return "error";
	}

	public String question(String prompt) {
		String chatAnswer = chat(prompt);
		System.out.println("gpt 응답: " + chatAnswer);
		return chatAnswer;
	}

	public String generateReport(String prompt) {
		ChatRequest request = ChatRequest.of(chatModel, prompt);
		request.messages().add(new Message(
			"system",
			"너는 산업안전 보고서를 작성하는 전문가야. 영어로 된 위험요인 자율점검표를 참고해서 " +
				"위험 지역 접근 수,보호구 미착용 수,건강 알림 수에 대한 안전 보고서를 작성해 " +
				"(상세히-> 반드시 보고서를 작성할 때는 영어로 된 자율점검표를 바탕으로 근거 있고 논리적으로 작성해." +
				"또한 보고서를 작성할 때 자율점검표의 어느 부분을 참고 해서 작성했는지 문단 앞에 배치하면서 서술해)."
				+ "보고서는 한국어로 작성해주고 마크다운('**')은 제거해줘"));
		try {
			ChatResponse response = template.postForObject(chatApiURL, request, ChatResponse.class);
			return response.choices().get(0).message().getContent();
		} catch (Exception e) {
			System.out.println("gpt에러: " + e);
		}
		return "error";
	}

	public String questionReport(String prompt) {
		String chatAnswer = generateReport(prompt);
		System.out.println("gpt 응답: " + chatAnswer);
		return chatAnswer;
	}
}
