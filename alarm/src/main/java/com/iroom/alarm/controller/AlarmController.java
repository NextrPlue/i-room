package com.iroom.alarm.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iroom.alarm.entity.Alarm;
import com.iroom.alarm.service.AlarmService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/alarms")
public class AlarmController {
	private final AlarmService alarmService;

	// 테스트용 알림 생성
	@PostMapping("/test")
	public ResponseEntity<String> createAlarm(@RequestBody Alarm alarm) {
		alarmService.handleAlarmEvent(
			alarm.getWorkerId(),
			alarm.getIncidentType(),
			alarm.getIncidentId(),
			alarm.getIncidentDescription()
		);
		return ResponseEntity.ok("Alarm created");
	}

	// WebSocket 테스트용 GET API
	@GetMapping("/test/send")
	public ResponseEntity<String> sendTestMessage() {
		Long workerId = 1L;
		String type = "위험요소";
		Long incidentId = 101L;
		String description = "작업자 침입 감지";

		alarmService.handleAlarmEvent(workerId, type, incidentId, description);
		return ResponseEntity.ok("WebSocket 메시지 전송 완료!");
	}

	@GetMapping("/workers/me")
	public ResponseEntity<List<Alarm>> getAlarmsForWorker(@RequestHeader("X-User-Id") Long workerId) {
		return ResponseEntity.ok(alarmService.getAlarmsForWorker(workerId));
	}

	@GetMapping("/admins")
	public ResponseEntity<List<Alarm>> getAlarmsForAdmin() {
		return ResponseEntity.ok(alarmService.getAlarmsForAdmin());
	}
}
