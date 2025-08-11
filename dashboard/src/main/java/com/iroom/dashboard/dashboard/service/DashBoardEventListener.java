package com.iroom.dashboard.dashboard.service;


import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iroom.dashboard.danger.service.AlarmEventHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DashBoardEventListener {
	private final ObjectMapper objectMapper;
	private final LocationEventHandler locationEventHandler;
	private final AlarmEventHandler alarmEventHandler;
	private final WorkerEventHandler workerEventHandler;
	@KafkaListener(topics = "iroom", groupId = "dashboard-service")
	public void handleLocationEvent(String message) {
		try {
			JsonNode eventNode = objectMapper.readTree(message);
			String eventType = eventNode.get("eventType").asText();
			JsonNode dataNode = eventNode.get("data");
			log.info("Received Kafka message: eventType={}, data={}", eventType, dataNode);
			// switch로 이벤트 구분
			switch (eventType) {
				case "WORKER_CREATED" -> workerEventHandler.handle(eventType,dataNode);
				case "PPE_VIOLATION" -> alarmEventHandler.handle(eventType,dataNode);
				case "WORKER_SENSOR_UPDATED", "HEAVY_EQUIPMENT_LOCATION_UPDATED"-> locationEventHandler.handle(eventType,dataNode);
				default -> log.warn("Unknown eventType: {}", eventType);
			}
		} catch (Exception e) {
			log.error("Failed to process Kafka message: {}", message, e);
		}
	}
}
