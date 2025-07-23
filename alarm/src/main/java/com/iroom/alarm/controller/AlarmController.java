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

    @GetMapping("/worker/{workerId}")
    public ResponseEntity<List<Alarm>> getAlarmsForWorker(@PathVariable Long workerId) {
        return ResponseEntity.ok(alarmService.getAlarmsForWorker(workerId));
    }

    @GetMapping("/admin")
    public ResponseEntity<List<Alarm>> getAlarmsForAdmin() {
        return ResponseEntity.ok(alarmService.getAlarmsForAdmin());
    }
}
