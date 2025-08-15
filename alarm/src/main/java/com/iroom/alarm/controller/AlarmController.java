package com.iroom.alarm.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iroom.alarm.entity.Alarm;
import com.iroom.alarm.service.AlarmService;
import com.iroom.modulecommon.dto.event.AlarmEvent;
import com.iroom.modulecommon.dto.response.ApiResponse;
import com.iroom.modulecommon.dto.response.PagedResponse;
import com.iroom.modulecommon.dto.response.SimpleResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/alarms")
public class AlarmController {
	private final AlarmService alarmService;

	// 보호구 탐지 AI 서버 전용 API
	@PostMapping("/ppe")
	public ResponseEntity<ApiResponse<SimpleResponse>> createAlarm(@RequestBody AlarmEvent alarmEvent) {
		SimpleResponse response = alarmService.handleAlarmEventFromApi(alarmEvent);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@GetMapping("/workers/me")
	public ResponseEntity<ApiResponse<PagedResponse<Alarm>>> getAlarmsForWorker(
		@RequestHeader("X-User-Id") Long workerId,
		@RequestParam(defaultValue = "0") Integer page,
		@RequestParam(defaultValue = "10") Integer size) {

		if (size > 50)
			size = 50;
		if (size < 0)
			size = 0;

		PagedResponse<Alarm> response = alarmService.getAlarmsForWorker(workerId, page, size);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@GetMapping("/admins")
	public ResponseEntity<ApiResponse<PagedResponse<Map<String, Object>>>> getAlarmsForAdmin(
		@RequestParam(defaultValue = "0") Integer page,
		@RequestParam(defaultValue = "10") Integer size,
		@RequestParam(defaultValue = "3") Integer hours) {

		if (size > 50)
			size = 50;
		if (size < 0)
			size = 0;
		if (hours < 1)
			hours = 1;
		if (hours > 168)
			hours = 168; // 최대 1주일(7일)

		PagedResponse<Map<String, Object>> response = alarmService.getAlarmsForAdmin(page, size, hours);
		return ResponseEntity.ok(ApiResponse.success(response));
	}
}
