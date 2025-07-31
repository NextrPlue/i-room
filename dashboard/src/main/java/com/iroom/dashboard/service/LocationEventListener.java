package com.iroom.dashboard.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationEventListener {
	private final ObjectMapper objectMapper;

	@KafkaListener(topics = "iroom", groupId = "dashboard-service")
	public void handleLocationEvent(String message) {
		try {
			JsonNode eventNode = objectMapper.readTree(message);
			String eventType = eventNode.get("eventType").asText();
			JsonNode dataNode = eventNode.get("data");

			log.info("Received Kafka message: eventType={}, data={}", eventType, dataNode);

			// WorkerEvent workerEvent = objectMapper.treeToValue(dataNode, WorkerEvent.class);
			//
			// switch (eventType) {
			// 	case "WORKER_CREATED" -> createWorkerReadModel(workerEvent);
			// 	case "WORKER_UPDATED" -> updateWorkerReadModel(workerEvent);
			// 	case "WORKER_DELETED" -> deleteWorkerReadModel(workerEvent.id());
			// 	default -> log.warn("Unknown event type: {}", eventType);
			// }
		} catch (Exception e) {
			log.error("Failed to process Kafka message: {}", message, e);
		}
	}
}
