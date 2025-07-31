package com.iroom.alarm.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iroom.alarm.entity.Alarm;
import com.iroom.alarm.repository.AlarmRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AlarmService {

	private final AlarmRepository alarmRepository;
	private final SimpMessagingTemplate messagingTemplate;

	// 알림을 생성해서 저장(Kafka 이벤트 수신 시 사용)
	public void handleAlarmEvent(Long workerId, String type, Long incidentId, String description) {
		Alarm alarm = Alarm.builder()
			.workerId(workerId)
			.incidentId(incidentId)
			.incidentType(type)
			.incidentDescription(description)
			.build();

		alarmRepository.save(alarm);

		// WebSocket 실시간 전송
		String message = String.format("[%s] %s", type, description);
		messagingTemplate.convertAndSend(
			"/topic/alarms",
			message
		);
	}

	// 근로자의 알림 목록을 조회
	public List<Alarm> getAlarmsForWorker(Long workerId) {
		return alarmRepository.findByWorkerIdOrderByOccuredAtDesc(workerId);
	}

	// 관리자용 전체 알림 목록을 조회
	public List<Alarm> getAlarmsForAdmin() {
		LocalDateTime time = LocalDateTime.now().minusHours(3);
		return alarmRepository.findByOccuredAtAfterOrderByOccuredAtDesc(time);
	}
}
