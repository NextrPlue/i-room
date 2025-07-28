package com.iroom.alarm.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iroom.alarm.controller.AlarmControllerTest.MockConfig;
import com.iroom.alarm.entity.Alarm;
import com.iroom.alarm.service.AlarmService;

@WebMvcTest(AlarmController.class)
@Import(MockConfig.class)
class AlarmControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private AlarmService alarmService;

	@TestConfiguration
	static class MockConfig {
		@Bean
		public AlarmService alarmService() {
			return Mockito.mock(AlarmService.class);
		}
	}

	@Test
	@DisplayName("알림 생성 성공")
	void createAlarmTest() throws Exception {
		// given
		Alarm alarm = Alarm.builder()
			.workerId(1L)
			.incidentType("DANGER")
			.incidentId(100L)
			.incidentDescription("위험 감지")
			.build();

		doNothing().when(alarmService).handleAlarmEvent(
			alarm.getWorkerId(),
			alarm.getIncidentType(),
			alarm.getIncidentId(),
			alarm.getIncidentDescription()
		);

		// when & then
		mockMvc.perform(post("/alarms/test")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(alarm)))
			.andExpect(status().isOk())
			.andExpect(content().string("Alarm created"));
	}

	@Test
	@DisplayName("WebSocket 테스트 메시지 전송 성공")
	void sendTestMessageTest() throws Exception {
		// given
		doNothing().when(alarmService).handleAlarmEvent(anyLong(), anyString(), anyLong(), anyString());

		// when & then
		mockMvc.perform(get("/alarms/test/send"))
			.andExpect(status().isOk())
			.andExpect(content().string("WebSocket 메시지 전송 완료!"));
	}

	@Test
	@DisplayName("근로자 알림 조회")
	void getAlarmsForWorkerTest() throws Exception {
		// given
		List<Alarm> alarms = List.of(
			Alarm.builder()
				.workerId(1L)
				.incidentType("DANGER")
				.incidentId(123L)
				.incidentDescription("작업자 침입")
				.build()
		);

		Mockito.when(alarmService.getAlarmsForWorker(1L)).thenReturn(alarms);

		// when & then
		mockMvc.perform(get("/alarms/workers/1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].incidentType").value("DANGER"));
	}

	@Test
	@DisplayName("관리자용 최근 알림 조회")
	void getAlarmsForAdminTest() throws Exception {
		// given
		List<Alarm> alarms = List.of(
			Alarm.builder()
				.workerId(2L)
				.incidentType("HEALTH")
				.incidentId(200L)
				.incidentDescription("심박 이상")
				.build()
		);

		Mockito.when(alarmService.getAlarmsForAdmin()).thenReturn(alarms);

		// when & then
		mockMvc.perform(get("/alarms/admins"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].incidentType").value("HEALTH"));
	}
}
