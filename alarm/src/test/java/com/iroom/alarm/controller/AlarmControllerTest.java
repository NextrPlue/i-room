package com.iroom.alarm.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.iroom.modulecommon.dto.response.ApiResponse;
import com.iroom.modulecommon.dto.response.PagedResponse;
import com.iroom.modulecommon.dto.response.SimpleResponse;
import com.iroom.modulecommon.exception.CustomException;
import com.iroom.modulecommon.exception.ErrorCode;

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
	@DisplayName("PPE API - 알림 생성 성공")
	void createAlarmFromPpeApiTest() throws Exception {
		// given
		AlarmEvent alarmEvent = new AlarmEvent(
			1L,
			LocalDateTime.now(),
			"PPE_VIOLATION",
			100L,
			37.5665,
			126.9780,
			"안전모 미착용 감지",
			"http://example.com/image.jpg"
		);

		SimpleResponse mockResponse = new SimpleResponse("알람이 발행되었습니다.");
		when(alarmService.handleAlarmEventFromApi(any(AlarmEvent.class))).thenReturn(mockResponse);

		// when & then
		mockMvc.perform(post("/alarms/ppe")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(alarmEvent)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("success"))
			.andExpect(jsonPath("$.data.message").value("알람이 발행되었습니다."));

		verify(alarmService, times(1)).handleAlarmEventFromApi(any(AlarmEvent.class));
	}

	@Test
	@DisplayName("근로자 알림 조회 성공")
	void getAlarmsForWorkerTest() throws Exception {
		// given
		List<Alarm> alarms = List.of(
			Alarm.builder()
				.workerId(1L)
				.occurredAt(LocalDateTime.now())
				.incidentType("PPE_VIOLATION")
				.incidentId(123L)
				.incidentDescription("안전모 미착용")
				.latitude(37.5665)
				.longitude(126.9780)
				.imageUrl("http://example.com/image.jpg")
				.build()
		);

		Page<Alarm> alarmPage = new PageImpl<>(alarms, PageRequest.of(0, 10), 1);
		PagedResponse<Alarm> pagedResponse = PagedResponse.of(alarmPage);
		Mockito.when(alarmService.getAlarmsForWorker(eq(1L), eq(0), eq(10))).thenReturn(pagedResponse);

		// when & then
		mockMvc.perform(get("/alarms/workers/me")
				.header("X-User-Id", "1")
				.param("page", "0")
				.param("size", "10"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("success"))
			.andExpect(jsonPath("$.data.content").isArray())
			.andExpect(jsonPath("$.data.content[0].workerId").value(1L))
			.andExpect(jsonPath("$.data.content[0].incidentType").value("PPE_VIOLATION"));

		verify(alarmService, times(1)).getAlarmsForWorker(1L, 0, 10);
	}

	@Test
	@DisplayName("관리자용 최근 3시간 알림 조회 성공")
	void getAlarmsForAdminTest() throws Exception {
		// given
		Map<String, Object> alarmMap = new HashMap<>();
		alarmMap.put("id", 1L);
		alarmMap.put("workerId", 2L);
		alarmMap.put("workerName", "홍길동");
		alarmMap.put("occurredAt", LocalDateTime.now());
		alarmMap.put("incidentType", "HEALTH_EMERGENCY");
		alarmMap.put("incidentId", 200L);
		alarmMap.put("incidentDescription", "심박수 비정상 증가");
		alarmMap.put("latitude", 37.5651);
		alarmMap.put("longitude", 126.9895);
		alarmMap.put("imageUrl", null);
		alarmMap.put("createdAt", LocalDateTime.now());

		List<Map<String, Object>> alarmMaps = List.of(alarmMap);
		Page<Map<String, Object>> alarmPage = new PageImpl<>(alarmMaps, PageRequest.of(0, 10), 1);
		PagedResponse<Map<String, Object>> pagedResponse = PagedResponse.of(alarmPage);
		Mockito.when(alarmService.getAlarmsForAdmin(eq(0), eq(10), eq(3))).thenReturn(pagedResponse);

		// when & then
		mockMvc.perform(get("/alarms/admins")
				.param("page", "0")
				.param("size", "10")
				.param("hours", "3"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("success"))
			.andExpect(jsonPath("$.data.content").isArray())
			.andExpect(jsonPath("$.data.content[0].workerId").value(2L))
			.andExpect(jsonPath("$.data.content[0].workerName").value("홍길동"))
			.andExpect(jsonPath("$.data.content[0].incidentType").value("HEALTH_EMERGENCY"))
			.andExpect(jsonPath("$.data.totalElements").value(1));

		verify(alarmService, times(1)).getAlarmsForAdmin(0, 10, 3);
	}

	@Test
	@DisplayName("PPE API - 알림 생성 실패 (근로자 찾을 수 없음)")
	void createAlarmFromPpeApi_workerNotFound() throws Exception {
		// given
		AlarmEvent alarmEvent = new AlarmEvent(
			999L,
			LocalDateTime.now(),
			"PPE_VIOLATION",
			100L,
			null,
			null,
			"안전모 미착용",
			null
		);

		when(alarmService.handleAlarmEventFromApi(any(AlarmEvent.class)))
			.thenThrow(new CustomException(ErrorCode.ALARM_WORKER_NOT_FOUND));

		// when & then
		mockMvc.perform(post("/alarms/ppe")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(alarmEvent)))
			.andExpect(status().isNotFound());

		verify(alarmService, times(1)).handleAlarmEventFromApi(any(AlarmEvent.class));
	}

	@Test
	@DisplayName("근로자 알림 조회 실패 (근로자 찾을 수 없음)")
	void getAlarmsForWorker_workerNotFound() throws Exception {
		// given
		when(alarmService.getAlarmsForWorker(eq(999L), anyInt(), anyInt()))
			.thenThrow(new CustomException(ErrorCode.ALARM_WORKER_NOT_FOUND));

		// when & then
		mockMvc.perform(get("/alarms/workers/me")
				.header("X-User-Id", "999"))
			.andExpect(status().isNotFound());

		verify(alarmService, times(1)).getAlarmsForWorker(999L, 0, 10);
	}

	@Test
	@DisplayName("파라미터 바운드 체크 - size 최대값 50 제한")
	void parameterBoundaryTest() throws Exception {
		// given
		PagedResponse<Alarm> mockResponse = PagedResponse.of(
			new PageImpl<>(List.of(), PageRequest.of(0, 50), 0));
		when(alarmService.getAlarmsForWorker(eq(1L), eq(0), eq(50))).thenReturn(mockResponse);

		// when & then - size가 50을 초과하면 50으로 제한
		mockMvc.perform(get("/alarms/workers/me")
				.header("X-User-Id", "1")
				.param("size", "100"))
			.andExpect(status().isOk());

		verify(alarmService, times(1)).getAlarmsForWorker(1L, 0, 50);
	}

	@Test
	@DisplayName("관리자 API - hours 파라미터 범위 테스트")
	void adminHoursParameterTest() throws Exception {
		// given
		PagedResponse<Map<String, Object>> mockResponse = PagedResponse.of(
			new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));
		when(alarmService.getAlarmsForAdmin(eq(0), eq(10), eq(168))).thenReturn(mockResponse);

		// when & then - hours가 168(1주일)을 초과하면 168로 제한
		mockMvc.perform(get("/alarms/admins")
				.param("hours", "200"))
			.andExpect(status().isOk());

		verify(alarmService, times(1)).getAlarmsForAdmin(0, 10, 168);
	}
}