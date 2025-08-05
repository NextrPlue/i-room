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
import com.iroom.sensor.dto.WorkerSensor.WorkerSensorUpdateRequest;
import com.iroom.sensor.dto.WorkerSensor.WorkerSensorUpdateResponse;
import com.iroom.sensor.dto.WorkerSensor.WorkerLocationResponse;
import com.iroom.sensor.service.WorkerSensorService;

@WebMvcTest(controllers = WorkerSensorController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
public class WorkerSensorControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private WorkerSensorService workerSensorService;

	@Test
	@DisplayName("POST /worker-sensor/update - 통합 센서 데이터 업데이트 테스트 (모든 데이터)")
	void updateWorkerSensorAllDataTest() throws Exception {
		// given
		Long workerId = 1L;
		Double latitude = 35.8343;
		Double longitude = 128.4723;
		Integer heartRate = 85;
		
		WorkerSensorUpdateRequest request = new WorkerSensorUpdateRequest(workerId, latitude, longitude, heartRate);
		WorkerSensorUpdateResponse response = new WorkerSensorUpdateResponse(workerId, latitude, longitude, heartRate);
		given(workerSensorService.updateSensor(request)).willReturn(response);

		// when & then
		mockMvc.perform(post("/worker-sensor/update")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.workerId").value(workerId))
			.andExpect(jsonPath("$.latitude").value(latitude))
			.andExpect(jsonPath("$.longitude").value(longitude))
			.andExpect(jsonPath("$.heartRate").value(heartRate));
	}

	@Test
	@DisplayName("POST /worker-sensor/update - 통합 센서 데이터 업데이트 테스트 (위치만)")
	void updateWorkerSensorLocationOnlyTest() throws Exception {
		// given
		Long workerId = 2L;
		Double latitude = 35.8343;
		Double longitude = 128.4723;
		Integer heartRate = null;
		
		WorkerSensorUpdateRequest request = new WorkerSensorUpdateRequest(workerId, latitude, longitude, heartRate);
		WorkerSensorUpdateResponse response = new WorkerSensorUpdateResponse(workerId, latitude, longitude, heartRate);
		given(workerSensorService.updateSensor(request)).willReturn(response);

		// when & then
		mockMvc.perform(post("/worker-sensor/update")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.workerId").value(workerId))
			.andExpect(jsonPath("$.latitude").value(latitude))
			.andExpect(jsonPath("$.longitude").value(longitude))
			.andExpect(jsonPath("$.heartRate").isEmpty());
	}

	@Test
	@DisplayName("POST /worker-sensor/update - 통합 센서 데이터 업데이트 테스트 (심박수만)")
	void updateWorkerSensorHeartRateOnlyTest() throws Exception {
		// given
		Long workerId = 3L;
		Double latitude = null;
		Double longitude = null;
		Integer heartRate = 90;
		
		WorkerSensorUpdateRequest request = new WorkerSensorUpdateRequest(workerId, latitude, longitude, heartRate);
		WorkerSensorUpdateResponse response = new WorkerSensorUpdateResponse(workerId, latitude, longitude, heartRate);
		given(workerSensorService.updateSensor(request)).willReturn(response);

		// when & then
		mockMvc.perform(post("/worker-sensor/update")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.workerId").value(workerId))
			.andExpect(jsonPath("$.latitude").isEmpty())
			.andExpect(jsonPath("$.longitude").isEmpty())
			.andExpect(jsonPath("$.heartRate").value(heartRate));
	}

	@Test
	@DisplayName("GET /worker-sensor/{workerId}/location - 위치 정보 조회 테스트")
	void getWorkerLocationTest() throws Exception {
		// given
		Long workerId = 4L;
		Double latitude = 35.8343;
		Double longitude = 128.4723;
		
		WorkerLocationResponse response = new WorkerLocationResponse(workerId, latitude, longitude);
		given(workerSensorService.getWorkerLocation(workerId)).willReturn(response);

		// when & then
		mockMvc.perform(get("/worker-sensor/{workerId}/location", workerId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.workerId").value(workerId))
			.andExpect(jsonPath("$.latitude").value(latitude))
			.andExpect(jsonPath("$.longitude").value(longitude));
	}
}