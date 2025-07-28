package com.iroom.sensor.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iroom.sensor.dto.WorkerHealth.WorkerUpdateLocationRequest;
import com.iroom.sensor.dto.WorkerHealth.WorkerUpdateLocationResponse;
import com.iroom.sensor.service.WorkerHealthService;

@WebMvcTest(WorkerHealthController.class)
public class WorkerHealthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private WorkerHealthService workerService;

	@Test
	@DisplayName("POST /worker-health/location - 위치 업데이트 테스트")
	void updateLocationTest() throws Exception {
		WorkerUpdateLocationRequest request = new WorkerUpdateLocationRequest(1L, "54.8343, 1.4723");
		WorkerUpdateLocationResponse response = new WorkerUpdateLocationResponse(1L, "54.8343, 1.4723");

		given(workerService.updateLocation(request)).willReturn(response);

		mockMvc.perform(post("/worker-health/location")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.workerId").value(1L))
			.andExpect(jsonPath("$.location").value("54.8343, 1.4723"));
	}
}
