package com.iroom.dashboard.pdf.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class OpenAIConfig {

	@Value("${openai.api.key}")
	String openaiApiKey;

	@Bean
	public RestTemplate template() {
		RestTemplate restTemplate = new RestTemplate();

		restTemplate.getInterceptors().add((request, body, execution) -> {

			//요청 헤더에 다음을 추가한 후 request를 보냄.
			request.getHeaders().add("Authorization", "Bearer " + openaiApiKey);
			return execution.execute(request, body);
		});

		return restTemplate;
	}
}
