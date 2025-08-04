package com.iroom.sensor.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iroom.sensor.dto.WorkerSensor.WorkerUpdateLocationRequest;
import com.iroom.sensor.dto.WorkerSensor.WorkerUpdateLocationResponse;
import com.iroom.sensor.dto.WorkerSensor.WorkerUpdateVitalSignsRequest;
import com.iroom.sensor.dto.WorkerSensor.WorkerUpdateVitalSignsResponse;
import com.iroom.sensor.service.WorkerSensorService;

@WebMvcTest(controllers = WorkerSensorController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
public class WorkerSensorControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private WorkerSensorService workerService;

	@Test
	@DisplayName("POST /worker-sensor/location - 위치 업데이트 테스트")
	void updateLocationTest() throws Exception {
		// given
		Double latitude = 35.8343;
		Double longitude = 128.4723;
		WorkerUpdateLocationRequest request = new WorkerUpdateLocationRequest(1L, latitude, longitude);
		WorkerUpdateLocationResponse response = new WorkerUpdateLocationResponse(1L, latitude, longitude);
		given(workerService.updateLocation(request)).willReturn(response);

		// when & then
		mockMvc.perform(post("/worker-sensor/location")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.workerId").value(1L))
			.andExpect(jsonPath("$.latitude").value(latitude))
			.andExpect(jsonPath("$.longitude").value(longitude));
	}

	@Test
	@DisplayName("POST /worker-sensor/vital-signs - 생체 정보 업데이트 테스트")
	void updateVitalSignsTest() throws Exception {
		// given
		WorkerUpdateVitalSignsRequest request = new WorkerUpdateVitalSignsRequest(2L, 88, 37.5F);
		WorkerUpdateVitalSignsResponse response = new WorkerUpdateVitalSignsResponse(2L, 88, 37.5F);
		given(workerService.updateVitalSigns(request)).willReturn(response);

		// when & then
		mockMvc.perform(post("/worker-sensor/vital-signs")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.workerId").value(2L))
			.andExpect(jsonPath("$.heartRate").value(88))
			.andExpect(jsonPath("$.bodyTemperature").value(37.5F));
	}

	@Test
	@DisplayName("GET /worker-sensor/{workerId}/location - 위치 조회 테스트")
	void getWorkerLocationTest() throws Exception {
		// given
		Long workerId = 3L;
		Double latitude = 35.8343;
		Double longitude = 128.4723;
		WorkerUpdateLocationResponse response = new WorkerUpdateLocationResponse(workerId, latitude, longitude);
		given(workerService.getWorkerLocation(workerId)).willReturn(response);

		// when & then
		mockMvc.perform(get("/worker-sensor/{workerId}/location", workerId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.workerId").value(workerId))
			.andExpect(jsonPath("$.latitude").value(latitude))
			.andExpect(jsonPath("$.longitude").value(longitude));
	}
}
