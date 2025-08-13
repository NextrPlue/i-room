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
			"You are an expert in writing industrial safety reports. "
				+ "Based on an English-language self-inspection checklist of risk factors, "
				+ "write a safety report on the number of approaches to hazardous areas, "
				+ "the number of instances of not wearing protective equipment, and the number of health alerts (in detail). "
				+ "When writing the report, you must base your analysis on the self-inspection checklist, providing logical and evidence-based reasoning. "
				+ "Also, at the beginning of each paragraph, indicate which part of the self-inspection checklist you are referring to, and then describe your findings. "
				+ "Remove any markdown (such as ‘**’), and write in English."));
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



	public String generateImprovement(String prompt) {
		ChatRequest request = ChatRequest.of(chatModel, prompt);
		request.messages().add(new Message(
			"system",
			"You are an expert in producing industrial environment improvement reports. "
				+ "Using an English-language hazard self-inspection checklist and data showing the daily number of occurrences by incident (hazard) type, "
				+ "write a detailed industrial environment improvement report. When writing the report, analyze using logical, evidence-based reasoning grounded in the self-inspection checklist. "
				+ "In addition, at the beginning of each paragraph, "
				+ "specify which part of the self-inspection checklist is being referenced and explain the results."
				+ "Remove any markdown (such as ‘**’), and write in English."));
		try {
			ChatResponse response = template.postForObject(chatApiURL, request, ChatResponse.class);
			return response.choices().get(0).message().getContent();
		} catch (Exception e) {
			System.out.println("gpt에러: " + e);
		}
		return "error";
	}


	public String questionImprovement(String prompt) {
		String chatAnswer = generateImprovement(prompt);
		System.out.println("gpt 응답: " + chatAnswer);
		return chatAnswer;
	}
}
