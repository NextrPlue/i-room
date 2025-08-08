package com.iroom.dashboard.service;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iroom.dashboard.entity.DashBoard;
import com.iroom.dashboard.repository.DashBoardRepository;
import com.iroom.modulecommon.dto.event.AlarmEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AlarmEventHandler {
	private final ObjectMapper objectMapper;
	private final DashBoardRepository dashBoardRepository;
	public void handle(String eventType, JsonNode dataNode) {
		try {
				switch (eventType) {
					case "PPE_VIOLATION" -> { //알람 메시지 수신
						LocalDate today = LocalDate.now();  // 오늘 날짜

						AlarmEvent alarmEvent = objectMapper.treeToValue(dataNode, AlarmEvent.class);
						String metricType = alarmEvent.incidentType();

						Optional<DashBoard> todayMetric = dashBoardRepository.findByMetricTypeAndRecordedAt(metricType, today);

						if (todayMetric.isPresent()) {
							DashBoard dashBoard = todayMetric.get();
							dashBoard.updateMetricValue();
							dashBoardRepository.save(dashBoard); // 명시적으로 save() 호출
						} else {
							DashBoard dashBoard = DashBoard.builder()
								.metricType(metricType)
								.metricValue(1)
								.build();
							dashBoardRepository.save(dashBoard);
						}
					}
				}
				log.info("Successfully processed alarm event: {}", eventType);
			}catch (Exception e) {
				log.error("Failed to process alarm event: {}", eventType, e);
			}
	}
}
