package com.iroom.alarm.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaEventListener {

	private final AlarmEventHandler alarmEventHandler;
	private final WorkerEventHandler workerEventHandler;
	private final ObjectMapper objectMapper;

	@KafkaListener(topics = "iroom", groupId = "alarm-service")
	public void handleEvent(String message) {
		try {
			JsonNode eventNode = objectMapper.readTree(message);
			String eventType = eventNode.get("eventType").asText();
			JsonNode dataNode = eventNode.get("data");

			log.info("Received Kafka message: eventType={}, data={}", eventType, dataNode);

			switch (eventType) {
				case "DANGER_ZONE", "HEALTH_RISK" -> alarmEventHandler.handle(eventType, dataNode);
				case "WORKER_CREATED", "WORKER_UPDATED", "WORKER_DELETED" ->
					workerEventHandler.handle(eventType, dataNode);
				default -> log.warn("Unknown event type: {}", eventType);
			}
		} catch (Exception e) {
			log.error("Failed to process Kafka message: {}", message, e);
		}
	}
}