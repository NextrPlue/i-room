package com.iroom.alarm.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.iroom.alarm.entity.Alarm;
import com.iroom.alarm.repository.AlarmRepository;

@ExtendWith(MockitoExtension.class)
class AlarmServiceTest {

	@Mock
	private AlarmRepository alarmRepository;

	@Mock
	private SimpMessagingTemplate messagingTemplate;

	@InjectMocks
	private AlarmService alarmService;

	private Alarm alarm;

	@BeforeEach
	void setUp() {
		alarm = Alarm.builder()
			.workerId(1L)
			.incidentId(101L)
			.incidentType("위험요소")
			.incidentDescription("작업자 침입 감지")
			.build();
	}

	@Test
	@DisplayName("알림 저장 및 WebSocket 전송 성공")
	void handleAlarmEvent_success() {
		// when
		alarmService.handleAlarmEvent(1L, "위험요소", 101L, "작업자 침입 감지");

		// then
		verify(alarmRepository, times(1)).save(any(Alarm.class));
		verify(messagingTemplate, times(1))
			.convertAndSend(eq("/topic/alarms"), contains("작업자 침입 감지"));
	}

	@Test
	@DisplayName("근로자 알림 조회 성공")
	void getAlarmsForWorker_success() {
		// given
		when(alarmRepository.findByWorkerIdOrderByOccuredAtDesc(1L))
			.thenReturn(List.of(alarm));

		// when
		List<Alarm> result = alarmService.getAlarmsForWorker(1L);

		// then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getWorkerId()).isEqualTo(1L);
	}

	@Test
	@DisplayName("관리자 알림 조회 성공 (최근 3시간 이내)")
	void getAlarmsForAdmin_success() {
		// given
		when(alarmRepository.findByOccuredAtAfterOrderByOccuredAtDesc(any()))
			.thenReturn(List.of(alarm));

		// when
		List<Alarm> result = alarmService.getAlarmsForAdmin();

		// then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getIncidentId()).isEqualTo(101L);
	}
}