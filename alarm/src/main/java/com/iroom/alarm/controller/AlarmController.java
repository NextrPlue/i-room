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
import com.iroom.modulecommon.dto.event.AlarmEvent;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/alarms")
public class AlarmController {
	private final AlarmService alarmService;

	// 보호구 탐지 AI 서버 전용 API
	@PostMapping("/ppe")
	public ResponseEntity<String> createAlarm(@RequestBody AlarmEvent alarmEvent) {
		alarmService.handleAlarmEvent(alarmEvent);
		return ResponseEntity.ok("Alarm created");
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
