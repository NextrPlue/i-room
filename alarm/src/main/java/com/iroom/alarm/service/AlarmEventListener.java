package com.iroom.alarm.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iroom.modulecommon.dto.event.AlarmEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AlarmEventListener {

	private final AlarmService alarmService;
	private final ObjectMapper objectMapper;


	@KafkaListener(topics = "iroom", groupId = "alarm-service")
	public void handleAlarmEvent(String message) {
		try {
			JsonNode eventNode = objectMapper.readTree(message);
			String eventType = eventNode.get("eventType").asText();
			JsonNode dataNode = eventNode.get("data");

			log.info("Received Kafka message: eventType={}, data={}", eventType, dataNode);

			if (eventType.equals("DANGER_AREA_ACCESS") || eventType.equals("HEALTH_ANOMALY")) {
				AlarmEvent alarmEvent = objectMapper.treeToValue(dataNode, AlarmEvent.class);
				alarmService.handleAlarmEvent(alarmEvent);
			} else {
				log.warn("Unknown event type: {}", eventType);
			}
		} catch (Exception e) {
			log.error("Failed to process Kafka message: {}", message, e);
		}
	}
}
