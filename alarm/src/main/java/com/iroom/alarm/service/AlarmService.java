package com.iroom.alarm.service;

import com.iroom.alarm.entity.Alarm;

import java.util.List;

public interface AlarmService {
    // 알림을 생성해서 저장(Kafka 이벤트 수신 시 사용)
    void handleAlarmEvent(Long workerId, String type, Long incidentId, String description);

    // 근로자의 알림 목록을 조회
    List<Alarm> getAlarmsForWorker(Long workerId);

    // 관리자용 전체 알림 목록을 조회
    List<Alarm> getAlarmsForAdmin();
}
