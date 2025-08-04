package com.iroom.dashboard.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iroom.modulecommon.dto.event.AlarmEvent;
import com.iroom.dashboard.dto.request.RiskManagementRequest;

import com.iroom.dashboard.util.DistanceUtil;
import com.iroom.modulecommon.service.KafkaProducerService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RiskManagementService {
	private final KafkaProducerService kafkaProducerService;

	public void detectDangerZone(RiskManagementRequest riskManagementRequest) {
		double radius = 10.0;

		Long workerId = 123L;
		LocalDateTime occuredAt = LocalDateTime.now();
		String incidentType = "HazardAccessDetected";
		Long incidentId = 456L;
		String workerLatitude = "37.5665";
		String workerLongitude = "126.97807";
		String incidentDescription = "Worker entered restricted hazard zone near entrance A";

		AlarmEvent alarmEvent = new AlarmEvent(
			workerId,           // workerId
			occuredAt,          // occuredAt
			incidentType,       // incidentType
			incidentId,         // incidentId
			workerLatitude,     // workerLatitude
			workerLongitude,    // workerLongitude
			incidentDescription // incidentDescription
		);
		double distance = DistanceUtil.calculateDistance(Double.parseDouble(riskManagementRequest.latitude()),
			Double.parseDouble(riskManagementRequest.longitude()),
			Double.parseDouble(workerLatitude),
			Double.parseDouble(workerLongitude));
		if (distance < radius) {
			kafkaProducerService.publishMessage("Hazard_Access_Detected", alarmEvent);
		}
	}
}
