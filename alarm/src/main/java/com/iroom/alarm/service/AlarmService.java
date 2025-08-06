package com.iroom.alarm.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iroom.alarm.config.StompHandler;
import com.iroom.alarm.entity.Alarm;
import com.iroom.alarm.repository.AlarmRepository;
import com.iroom.modulecommon.dto.event.AlarmEvent;
import com.iroom.modulecommon.dto.response.PagedResponse;
import com.iroom.modulecommon.dto.response.SimpleResponse;
import com.iroom.modulecommon.service.KafkaProducerService;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AlarmService {

	private final AlarmRepository alarmRepository;
	private final SimpMessagingTemplate messagingTemplate;
	private final StompHandler stompHandler;
	private final KafkaProducerService kafkaProducerService;

	// 외부 API 호출용
	@PreAuthorize("hasAuthority('ROLE_PPE_SYSTEM')")
	public SimpleResponse handleAlarmEventFromApi(AlarmEvent alarmEvent) {
		processAlarmEvent(alarmEvent);
		kafkaProducerService.publishMessage("PPE_VIOLATION", alarmEvent);

		return new SimpleResponse("알람이 발행되었습니다.");
	}

	// 내부 Kafka 이벤트 수신용
	public void handleAlarmEvent(AlarmEvent alarmEvent) {
		processAlarmEvent(alarmEvent);
	}

	// 실제 알림 처리 로직
	private void processAlarmEvent(AlarmEvent alarmEvent) {
		Alarm alarm = Alarm.builder()
			.workerId(alarmEvent.workerId())
			.occurredAt(alarmEvent.occurredAt())
			.incidentType(alarmEvent.incidentType())
			.incidentId(alarmEvent.incidentId())
			.latitude(alarmEvent.workerLatitude())
			.longitude(alarmEvent.workerLongitude())
			.incidentDescription(alarmEvent.incidentDescription())
			.imageUrl(alarmEvent.workerImageUrl())
			.build();

		alarmRepository.save(alarm);

		// WebSocket 실시간 전송
		String adminMessage = String.format("[%s] %s (작업자 ID: %d)", alarmEvent.incidentType(),
			alarmEvent.incidentDescription(), alarmEvent.workerId());
		if (alarmEvent.workerImageUrl() != null) {
			adminMessage += " (" + alarmEvent.workerImageUrl() + ")";
		}
		String workerMessage = String.format("[%s] %s", alarmEvent.incidentType(), alarmEvent.incidentDescription());

		// 관리자에게 모든 알람 전송
		messagingTemplate.convertAndSend("/topic/alarms/admin", adminMessage);

		// 해당 근로자에게만 개별 알람 전송
		String sessionId = stompHandler.getSessionIdByUserId(alarmEvent.workerId().toString());
		if (sessionId != null) {
			String workerDestination = "/queue/alarms-" + sessionId;
			messagingTemplate.convertAndSend(workerDestination, workerMessage);
		}
	}

	// 근로자의 알림 목록을 조회
	@PreAuthorize("hasAuthority('ROLE_WORKER') and #workerId == authentication.principal")
	public PagedResponse<Alarm> getAlarmsForWorker(Long workerId, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<Alarm> alarmPage = alarmRepository.findByWorkerIdOrderByOccurredAtDesc(workerId, pageable);

		return PagedResponse.of(alarmPage);
	}

	// 관리자용 전체 알림 목록을 조회
	@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_READER')")
	public PagedResponse<Alarm> getAlarmsForAdmin(int page, int size, int hours) {
		Pageable pageable = PageRequest.of(page, size);
		LocalDateTime time = LocalDateTime.now().minusHours(hours);
		Page<Alarm> alarmPage = alarmRepository.findByOccurredAtAfterOrderByOccurredAtDesc(time, pageable);

		return PagedResponse.of(alarmPage);
	}
}
