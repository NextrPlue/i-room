package com.iroom.dashboard.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iroom.dashboard.entity.DangerArea;
import com.iroom.dashboard.entity.Incident;
import com.iroom.dashboard.repository.DangerAreaRepository;
import com.iroom.dashboard.repository.IncidentRepository;
import com.iroom.dashboard.util.DistanceUtil;
import com.iroom.modulecommon.dto.event.AlarmEvent;
import com.iroom.modulecommon.service.KafkaProducerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationEventListener {
	private final ObjectMapper objectMapper;
	private final Map<String, Map<String, String>> workerCache = new HashMap<>();
	private final Map<String, Map<String, String>> equipmentCache = new HashMap<>();
	private final DangerAreaRepository dangerAreaRepository;
	private final KafkaProducerService kafkaProducerService;
	private final IncidentRepository incidentRepository;

	@KafkaListener(topics = "iroom", groupId = "dashboard-service")
	public void handleLocationEvent(String message) {
		try {
			JsonNode eventNode = objectMapper.readTree(message);
			String eventType = eventNode.get("eventType").asText();
			JsonNode dataNode = eventNode.get("data");

			log.info("Received Kafka message: eventType={}, data={}", eventType, dataNode);

			// 캐시 초기화 또는 불러오기

			// switch로 이벤트 구분
			switch (eventType) {
				case "WORKER_SENSOR_UPDATED" -> {
					Map<String, String> info = new HashMap<>();
					System.out.println("Worker_Location 발행");
					double radius = 10.0;
					Long workerId = dataNode.get("workerId").asLong();
					LocalDateTime occurredAt = LocalDateTime.now();
					String incidentType = "위험 구역 접근";

					String workerLatitude = dataNode.get("latitude").asText();
					String workerLongitude = dataNode.get("longitude").asText();

					List<DangerArea> areas = dangerAreaRepository.findAll();
					for (DangerArea area : areas) {
						double distance = DistanceUtil.calculateDistance(Double.parseDouble(workerLatitude),
							Double.parseDouble(workerLongitude),
							area.getLatitude(),
							area.getLongitude());
						if (distance < radius) {
							String incidentDescription = "Worker entered restricted hazard zone near latitude: " +
								area.getLatitude() + " longitude: " + area.getLongitude();
							Incident incident = Incident.builder().
								workerId(workerId).
								occurredAt(occurredAt).
								incidentType(incidentType).
								latitude(Double.valueOf(workerLatitude)).
								longitude(Double.valueOf(workerLongitude)).
								incidentDescription(incidentDescription).
								build();
							incidentRepository.save(incident);
							Long incidentId = incidentRepository.findLatestIncidentId();
							AlarmEvent alarmEvent = new AlarmEvent(
								workerId,
								occurredAt,
								incidentType,
								incidentId,
								Double.valueOf(workerLatitude),
								Double.valueOf(workerLongitude),
								incidentDescription,
								null
							);

							kafkaProducerService.publishMessage("DANGER_AREA_ACCESS", alarmEvent);
						}
					}
					if (dataNode.hasNonNull("latitude")) {
						info.put("workerLatitude", dataNode.get("latitude").asText());
					}
					if (dataNode.hasNonNull("longitude")) {
						info.put("workerLongitude", dataNode.get("longitude").asText());
					}
					workerCache.put(dataNode.get("workerId").asText(), info);
					// workerCache.put("workerReceived", true);
				}
				case "HEAVY_EQUIPMENT_LOCATION_UPDATED" -> {
					Map<String, String> info = new HashMap<>();
					System.out.println("Equipment_Location 발행");

					if (dataNode.hasNonNull("latitude")) {
						info.put("equipmentLatitude", dataNode.get("latitude").asText());
					}
					if (dataNode.hasNonNull("longitude")) {
						info.put("equipmentLongitude", dataNode.get("longitude").asText());
					}
					if (dataNode.hasNonNull("radius")) {
						info.put("equipmentRadius", dataNode.get("radius").asText());
					}
					equipmentCache.put(dataNode.get("equipmentId").asText(), info);
				}
				default -> {
					log.warn("Unknown eventType: {}", eventType);
					return;
				}
			}
			for (Map.Entry<String, Map<String, String>> workerEntry : workerCache.entrySet()) {
				for (Map.Entry<String, Map<String, String>> equipmentEntry : equipmentCache.entrySet()) {
					double distance = DistanceUtil.calculateDistance(
						Double.parseDouble(workerEntry.getValue().get("workerLatitude")),
						Double.parseDouble(workerEntry.getValue().get("workerLongitude")),
						Double.parseDouble(equipmentEntry.getValue().get("equipmentLatitude")),
						Double.parseDouble(equipmentEntry.getValue().get("equipmentLongitude")));
					System.out.println("거리:  " + distance);
					//위험거리 접근시 알람 서비스에 메시지 발행
					if (distance < Double.parseDouble(equipmentEntry.getValue().get("equipmentRadius"))) {

						Long workerId = Long.valueOf(workerEntry.getKey());
						LocalDateTime occurredAt = LocalDateTime.now();
						String incidentType = "충돌 위험";

						Double latitude = Double.valueOf(workerEntry.getValue().get("workerLatitude"));
						Double longitude = Double.valueOf(workerEntry.getValue().get("workerLongitude"));
						String incidentDescription = "Worker entered restricted hazard zone";
						Incident incident = Incident.builder().
							workerId(workerId).
							occurredAt(occurredAt).
							incidentType(incidentType).
							latitude(latitude).
							longitude(longitude).
							incidentDescription(incidentDescription).
							build();
						incidentRepository.save(incident);
						Long incidentId = incidentRepository.findLatestIncidentId();
						AlarmEvent alarmEvent = new AlarmEvent(
							workerId,
							occurredAt,
							incidentType,
							incidentId,
							latitude,
							longitude,
							incidentDescription,
							null
						);
						System.out.println("DANGER_AREA 메시지 발행");
						kafkaProducerService.publishMessage("DANGER_AREA_ACCESS", alarmEvent);
					}
				}
			}

		} catch (Exception e) {
			log.error("Failed to process Kafka message: {}", message, e);
		}
	}
}
