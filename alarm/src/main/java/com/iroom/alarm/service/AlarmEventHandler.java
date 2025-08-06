package com.iroom.alarm.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iroom.modulecommon.dto.event.AlarmEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AlarmEventHandler {

	private final AlarmService alarmService;
	private final ObjectMapper objectMapper;

	public void handle(String eventType, JsonNode dataNode) {
		try {
			AlarmEvent alarmEvent = objectMapper.treeToValue(dataNode, AlarmEvent.class);
			alarmService.handleAlarmEvent(alarmEvent);
			log.info("Successfully processed alarm event: {}", eventType);
		} catch (Exception e) {
			log.error("Failed to process alarm event: {}", eventType, e);
		}
	}
}