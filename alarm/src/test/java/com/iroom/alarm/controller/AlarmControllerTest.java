package com.iroom.alarm.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iroom.alarm.entity.Alarm;
import com.iroom.alarm.service.AlarmService;
import com.iroom.modulecommon.dto.event.AlarmEvent;
import com.iroom.modulecommon.dto.response.PagedResponse;
import com.iroom.modulecommon.dto.response.SimpleResponse;

@WebMvcTest(value = AlarmController.class, excludeAutoConfiguration = {
	org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
class AlarmControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private AlarmService alarmService;

	@Test
	@DisplayName("알림 생성 성공")
	void createAlarmTest() throws Exception {
		// given
		AlarmEvent alarmEvent = new AlarmEvent(
			1L,
			LocalDateTime.now(),
			"DANGER",
			100L,
			null,
			null,
			"위험 감지",
			null
		);

		SimpleResponse mockResponse = new SimpleResponse("알림이 발행되었습니다.");
		when(alarmService.handleAlarmEventFromApi(any(AlarmEvent.class))).thenReturn(mockResponse);

		// when & then
		mockMvc.perform(post("/alarms/ppe")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(alarmEvent)))
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName("근로자 알림 조회")
	void getAlarmsForWorkerTest() throws Exception {
		// given
		List<Alarm> alarms = List.of(
			Alarm.builder()
				.workerId(1L)
				.occurredAt(LocalDateTime.now())
				.incidentType("DANGER")
				.incidentId(123L)
				.incidentDescription("작업자 침입")
				.build()
		);

		Page<Alarm> alarmPage = new PageImpl<>(alarms, PageRequest.of(0, 10), 1);
		PagedResponse<Alarm> pagedResponse = PagedResponse.of(alarmPage);
		Mockito.when(alarmService.getAlarmsForWorker(eq(1L), anyInt(), anyInt())).thenReturn(pagedResponse);

		// when & then
		mockMvc.perform(get("/alarms/workers/me")
				.header("X-User-Id", "1"))
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName("관리자용 최근 알림 조회")
	void getAlarmsForAdminTest() throws Exception {
		// given
		List<Alarm> alarms = List.of(
			Alarm.builder()
				.workerId(2L)
				.occurredAt(LocalDateTime.now())
				.incidentType("HEALTH")
				.incidentId(200L)
				.incidentDescription("심박 이상")
				.build()
		);

		Page<Alarm> alarmPage = new PageImpl<>(alarms, PageRequest.of(0, 10), 1);
		PagedResponse<Alarm> pagedResponse = PagedResponse.of(alarmPage);
		Mockito.when(alarmService.getAlarmsForAdmin(anyInt(), anyInt(), anyInt())).thenReturn(pagedResponse);

		// when & then
		mockMvc.perform(get("/alarms/admins"))
			.andExpect(status().isOk());
	}
}