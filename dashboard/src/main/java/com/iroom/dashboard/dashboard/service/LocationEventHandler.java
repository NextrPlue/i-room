package com.iroom.dashboard.dashboard.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iroom.dashboard.danger.entity.DangerArea;
import com.iroom.dashboard.dashboard.entity.Incident;
import com.iroom.dashboard.dashboard.entity.WorkerInfoReadModel;
import com.iroom.dashboard.danger.repository.DangerAreaRepository;
import com.iroom.dashboard.dashboard.repository.IncidentRepository;
import com.iroom.dashboard.dashboard.repository.WorkerInfoReadModelRepository;
import com.iroom.dashboard.danger.util.DistanceUtil;
import com.iroom.modulecommon.dto.event.AlarmEvent;
import com.iroom.modulecommon.dto.event.WorkerSensorEvent;
import com.iroom.modulecommon.exception.CustomException;
import com.iroom.modulecommon.exception.ErrorCode;
import com.iroom.modulecommon.service.KafkaProducerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Transactional
@Slf4j
public class LocationEventHandler {
	private final Map<String, Map<String, String>> workerCache = new HashMap<>();
	private final Map<String, Map<String, String>> equipmentCache = new HashMap<>();
	private final DangerAreaRepository dangerAreaRepository;
	private final WorkerInfoReadModelRepository workerInfoReadModelRepository;
	private final KafkaProducerService kafkaProducerService;
	private final IncidentRepository incidentRepository;
	private final ObjectMapper objectMapper;

	public void handle(String eventType, JsonNode dataNode) {
		try {
			switch (eventType) {
				case "WORKER_SENSOR_UPDATED" -> {
					WorkerSensorEvent workerSensorEvent = objectMapper.treeToValue(dataNode, WorkerSensorEvent.class);
					updateWorkerInfoReadModel(workerSensorEvent);
					Map<String, String> info = new HashMap<>();
					System.out.println("Worker_Location 발행");
					Long workerId = dataNode.get("workerId").asLong();
					LocalDateTime occurredAt = LocalDateTime.now();

					String workerLatitude = dataNode.get("latitude").asText();
					String workerLongitude = dataNode.get("longitude").asText();

					List<DangerArea> areas = dangerAreaRepository.findAll();
					for (DangerArea area : areas) {
						// 디버깅용 중심점까지의 거리 계산
						double distance = DistanceUtil.calculateDistance(Double.parseDouble(workerLatitude),
							Double.parseDouble(workerLongitude),
							area.getLatitude(),
							area.getLongitude());

						// 사각형 위험구역 내부에 있는지 판정
						boolean isInsideDangerArea = DistanceUtil.isPointInsideRectangleArea(
							Double.parseDouble(workerLatitude),
							Double.parseDouble(workerLongitude),
							area.getLatitude(),
							area.getLongitude(),
							area.getWidth(),
							area.getHeight()
						);

						System.out.println("위험구역 '" + area.getName() + "' 중심거리: " + distance + "m, " +
							"크기: " + area.getWidth() + "×" + area.getHeight() + "m, " +
							"영역내부: " + isInsideDangerArea + ", 사용자 ID: " + workerId);

						if (isInsideDangerArea) {
							String incidentDescription = "위험구역 접근 발생으로 인한 오류";
							String incidentType = "DANGER_ZONE";
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

							kafkaProducerService.publishMessage("DANGER_ZONE", alarmEvent);
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
			}

			//건설 장비 접근 검사
			for (Map.Entry<String, Map<String, String>> workerEntry : workerCache.entrySet()) {
				for (Map.Entry<String, Map<String, String>> equipmentEntry : equipmentCache.entrySet()) {
					double distance = DistanceUtil.calculateDistance(
						Double.parseDouble(workerEntry.getValue().get("workerLatitude")),
						Double.parseDouble(workerEntry.getValue().get("workerLongitude")),
						Double.parseDouble(equipmentEntry.getValue().get("equipmentLatitude")),
						Double.parseDouble(equipmentEntry.getValue().get("equipmentLongitude")));
					System.out.println("거리:  " + distance);
					//위험거리 접근시 알람 서비스에 메시지 발행
					if (distance < Double.valueOf(equipmentEntry.getValue().get("equipmentRadius"))) {

						Long workerId = Long.valueOf(workerEntry.getKey());
						LocalDateTime occurredAt = LocalDateTime.now();
						String incidentType = "DANGER_ZONE";

						Double latitude = Double.valueOf(workerEntry.getValue().get("workerLatitude"));
						Double longitude = Double.valueOf(workerEntry.getValue().get("workerLongitude"));
						String incidentDescription = "건설장비 접근 발생으로 인한 오류";
						Incident incident = Incident.builder().
							workerId(workerId).
							occurredAt(occurredAt).
							incidentType(incidentType).
							latitude(Double.valueOf(latitude)).
							longitude(Double.valueOf(longitude)).
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
						kafkaProducerService.publishMessage("DANGER_ZONE", alarmEvent);
					}
				}
			}

		} catch (Exception e) {
			log.error("Failed to process location event: {}", eventType, e);
		}
	}

	public void updateWorkerInfoReadModel(WorkerSensorEvent event) {
		WorkerInfoReadModel readModel = workerInfoReadModelRepository.findById(event.workerId())
			.orElseThrow(() -> new CustomException(ErrorCode.MANAGEMENT_WORKER_NOT_FOUND));
		readModel.updateFromEvent(event);
		workerInfoReadModelRepository.save(readModel);
	}
}
