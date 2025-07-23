package com.iroom.alarm.service;

import com.iroom.alarm.entity.Alarm;
import com.iroom.alarm.repository.AlarmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlarmServiceImpl implements AlarmService {

    private final AlarmRepository alarmRepository;

    @Transactional
    @Override
    public void handleAlarmEvent(Long workerId, String type, Long incidentId, String description) {
        Alarm alarm = Alarm.builder()
                .workerId(workerId)
                .incidentId(incidentId)
                .incidentType(type)
                .incidentDescription(description)
                .build();

        alarmRepository.save(alarm);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Alarm> getAlarmsForWorker(Long workerId) {
        return alarmRepository.findByWorkerIdOrderByOccuredAtDesc(workerId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Alarm> getAlarmsForAdmin() {
        LocalDateTime time = LocalDateTime.now().minusHours(3);
        return alarmRepository.findByOccuredAtAfterOrderByOccuredAtDesc(time);
    }
}
