package com.iroom.alarm.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.iroom.alarm.config.StompHandler;
import com.iroom.alarm.entity.Alarm;
import com.iroom.alarm.entity.WorkerReadModel;
import com.iroom.alarm.repository.AlarmRepository;
import com.iroom.alarm.repository.WorkerReadModelRepository;
import com.iroom.modulecommon.dto.event.AlarmEvent;
import com.iroom.modulecommon.dto.response.PagedResponse;
import com.iroom.modulecommon.dto.response.SimpleResponse;
import com.iroom.modulecommon.exception.CustomException;
import com.iroom.modulecommon.exception.ErrorCode;
import com.iroom.modulecommon.service.KafkaProducerService;

@ExtendWith(MockitoExtension.class)
class AlarmServiceTest {

	@Mock
	private AlarmRepository alarmRepository;

	@Mock
	private SimpMessagingTemplate messagingTemplate;

	@Mock
	private StompHandler stompHandler;

	@Mock
	private KafkaProducerService kafkaProducerService;

	@Mock
	private WorkerReadModelRepository workerReadModelRepository;

	@InjectMocks
	private AlarmService alarmService;

	private Alarm alarm;
	private WorkerReadModel workerReadModel;

	@BeforeEach
	void setUp() {
		alarm = Alarm.builder()
			.workerId(1L)
			.occurredAt(LocalDateTime.now())
			.incidentId(101L)
			.incidentType("위험요소")
			.incidentDescription("작업자 침입 감지")
			.build();

		workerReadModel = WorkerReadModel.builder()
			.id(1L)
			.name("테스트 근로자")
			.email("test@example.com")
			.build();
	}

	@Test
	@DisplayName("알림 저장 및 WebSocket 전송 성공")
	void handleAlarmEvent_success() {
		// given
		AlarmEvent alarmEvent = new AlarmEvent(
			1L,
			LocalDateTime.now(),
			"위험요소",
			101L,
			null,
			null,
			"작업자 침입 감지",
			null
		);
		when(workerReadModelRepository.findById(1L)).thenReturn(Optional.of(workerReadModel));
		when(stompHandler.getSessionIdByUserId("1")).thenReturn("session123");

		// when
		alarmService.handleAlarmEvent(alarmEvent);

		// then
		verify(workerReadModelRepository, times(1)).findById(1L);
		verify(alarmRepository, times(1)).save(any(Alarm.class));
		verify(messagingTemplate, times(1))
			.convertAndSend(eq("/topic/alarms/admin"), contains("작업자 침입 감지"));
		verify(messagingTemplate, times(1))
			.convertAndSend(eq("/queue/alarms-session123"), contains("작업자 침입 감지"));
	}

	@Test
	@DisplayName("근로자 알림 조회 성공")
	void getAlarmsForWorker_success() {
		// given
		Pageable pageable = PageRequest.of(0, 10);
		Page<Alarm> alarmPage = new PageImpl<>(List.of(alarm), pageable, 1);
		when(workerReadModelRepository.findById(1L)).thenReturn(Optional.of(workerReadModel));
		when(alarmRepository.findByWorkerIdOrderByOccurredAtDesc(1L, pageable))
			.thenReturn(alarmPage);

		// when
		PagedResponse<Alarm> result = alarmService.getAlarmsForWorker(1L, 0, 10);

		// then
		verify(workerReadModelRepository, times(1)).findById(1L);
		assertThat(result.content()).hasSize(1);
		assertThat(result.content().get(0).getWorkerId()).isEqualTo(1L);
	}

	@Test
	@DisplayName("관리자 알림 조회 성공 (최근 3시간 이내)")
	void getAlarmsForAdmin_success() {
		// given
		Pageable pageable = PageRequest.of(0, 10);
		Page<Alarm> alarmPage = new PageImpl<>(List.of(alarm), pageable, 1);
		when(alarmRepository.findByOccurredAtAfterOrderByOccurredAtDesc(any(LocalDateTime.class), eq(pageable)))
			.thenReturn(alarmPage);

		// when
		PagedResponse<Alarm> result = alarmService.getAlarmsForAdmin(0, 10, 3);

		// then
		assertThat(result.content()).hasSize(1);
		assertThat(result.content().get(0).getIncidentId()).isEqualTo(101L);
	}

	@Test
	@DisplayName("존재하지 않는 근로자 - handleAlarmEvent 실패")
	void handleAlarmEvent_workerNotFound_throwsException() {
		// given
		AlarmEvent alarmEvent = new AlarmEvent(
			999L,
			LocalDateTime.now(),
			"위험요소",
			101L,
			null,
			null,
			"작업자 침입 감지",
			null
		);
		when(workerReadModelRepository.findById(999L)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> alarmService.handleAlarmEvent(alarmEvent))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(ErrorCode.ALARM_WORKER_NOT_FOUND.getMessage());

		verify(workerReadModelRepository, times(1)).findById(999L);
		verify(alarmRepository, never()).save(any(Alarm.class));
		verify(messagingTemplate, never()).convertAndSend(anyString(), anyString());
	}

	@Test
	@DisplayName("존재하지 않는 근로자 - getAlarmsForWorker 실패")
	void getAlarmsForWorker_workerNotFound_throwsException() {
		// given
		when(workerReadModelRepository.findById(999L)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> alarmService.getAlarmsForWorker(999L, 0, 10))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(ErrorCode.ALARM_WORKER_NOT_FOUND.getMessage());

		verify(workerReadModelRepository, times(1)).findById(999L);
		verify(alarmRepository, never()).findByWorkerIdOrderByOccurredAtDesc(anyLong(), any(Pageable.class));
	}

	@Test
	@DisplayName("PPE API 호출 - handleAlarmEventFromApi 성공")
	void handleAlarmEventFromApi_success() {
		// given
		AlarmEvent alarmEvent = new AlarmEvent(
			1L,
			LocalDateTime.now(),
			"PPE_VIOLATION",
			101L,
			null,
			null,
			"안전모 미착용",
			null
		);
		when(workerReadModelRepository.findById(1L)).thenReturn(Optional.of(workerReadModel));
		when(stompHandler.getSessionIdByUserId("1")).thenReturn("session123");

		// when
		SimpleResponse result = alarmService.handleAlarmEventFromApi(alarmEvent);

		// then
		assertThat(result.message()).isEqualTo("알람이 발행되었습니다.");
		verify(workerReadModelRepository, times(1)).findById(1L);
		verify(alarmRepository, times(1)).save(any(Alarm.class));
		verify(kafkaProducerService, times(1)).publishMessage("PPE_VIOLATION", alarmEvent);
		verify(messagingTemplate, times(1))
			.convertAndSend(eq("/topic/alarms/admin"), contains("안전모 미착용"));
		verify(messagingTemplate, times(1))
			.convertAndSend(eq("/queue/alarms-session123"), contains("안전모 미착용"));
	}
}