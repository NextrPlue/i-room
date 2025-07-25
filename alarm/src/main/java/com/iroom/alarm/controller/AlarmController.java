package com.iroom.alarm.controller;

import com.iroom.alarm.entity.Alarm;
import com.iroom.alarm.service.AlarmService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/worker/{workerId}")
    public ResponseEntity<List<Alarm>> getAlarmsForWorker(@PathVariable Long workerId) {
        return ResponseEntity.ok(alarmService.getAlarmsForWorker(workerId));
    }

    @GetMapping("/admin")
    public ResponseEntity<List<Alarm>> getAlarmsForAdmin() {
        return ResponseEntity.ok(alarmService.getAlarmsForAdmin());
    }
}
