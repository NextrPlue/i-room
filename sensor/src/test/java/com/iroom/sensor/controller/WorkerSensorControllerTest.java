package com.iroom.sensor.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.iroom.sensor.dto.WorkerSensor.WorkerSensorUpdateResponse;
import com.iroom.sensor.dto.WorkerSensor.WorkerLocationResponse;
import com.iroom.sensor.service.WorkerSensorService;
import com.iroom.modulecommon.dto.response.ApiResponse;

@WebMvcTest(controllers = WorkerSensorController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
public class WorkerSensorControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private WorkerSensorService workerSensorService;

	@Test
	@DisplayName("PUT /worker-sensor/update - 센서 데이터 업데이트 테스트")
	void updateWorkerSensorAllDataTest() throws Exception {
		// given
		Long workerId = 1L;
		Double latitude = 35.8343;
		Double longitude = 128.4723;
		Double heartRate = 85.0;
		Long steps = 1000L;
		Double speed = 5.5;
		Double pace = 10.9;
		Long stepPerMinute = 120L;

		byte[] binaryData = createBinaryData(latitude, longitude, heartRate, steps, speed, pace, stepPerMinute);
		WorkerSensorUpdateResponse response = new WorkerSensorUpdateResponse(workerId, latitude, longitude, heartRate,
			steps, speed, pace, stepPerMinute);
		given(workerSensorService.updateSensor(workerId, binaryData)).willReturn(response);

		// when & then
		mockMvc.perform(put("/worker-sensor/update")
				.header("X-User-Id", workerId)
				.contentType("application/octet-stream")
				.content(binaryData))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("success"))
			.andExpect(jsonPath("$.data.workerId").value(workerId))
			.andExpect(jsonPath("$.data.latitude").value(latitude))
			.andExpect(jsonPath("$.data.longitude").value(longitude))
			.andExpect(jsonPath("$.data.heartRate").value(heartRate))
			.andExpect(jsonPath("$.data.steps").value(steps))
			.andExpect(jsonPath("$.data.speed").value(speed))
			.andExpect(jsonPath("$.data.pace").value(pace))
			.andExpect(jsonPath("$.data.stepPerMinute").value(stepPerMinute));
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
			.andExpect(jsonPath("$.status").value("success"))
			.andExpect(jsonPath("$.data.workerId").value(workerId))
			.andExpect(jsonPath("$.data.latitude").value(latitude))
			.andExpect(jsonPath("$.data.longitude").value(longitude));
	}

	@Test
	@DisplayName("POST /worker-sensor/locations - 다중 근로자 위치 조회 성공")
	void getWorkersLocationTest() throws Exception {
		// given
		List<Long> workerIds = Arrays.asList(1L, 2L, 3L);
		
		List<WorkerLocationResponse> locationResponses = Arrays.asList(
			new WorkerLocationResponse(1L, 37.5665, 126.9780),
			new WorkerLocationResponse(2L, 37.5666, 126.9781),
			new WorkerLocationResponse(3L, 37.5667, 126.9782)
		);
		
		given(workerSensorService.getWorkersLocation(workerIds)).willReturn(locationResponses);

		// when & then
		mockMvc.perform(post("/worker-sensor/locations")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(workerIds)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("success"))
			.andExpect(jsonPath("$.data.length()").value(3))
			.andExpect(jsonPath("$.data[0].workerId").value(1L))
			.andExpect(jsonPath("$.data[0].latitude").value(37.5665))
			.andExpect(jsonPath("$.data[0].longitude").value(126.9780))
			.andExpect(jsonPath("$.data[1].workerId").value(2L))
			.andExpect(jsonPath("$.data[1].latitude").value(37.5666))
			.andExpect(jsonPath("$.data[1].longitude").value(126.9781))
			.andExpect(jsonPath("$.data[2].workerId").value(3L))
			.andExpect(jsonPath("$.data[2].latitude").value(37.5667))
			.andExpect(jsonPath("$.data[2].longitude").value(126.9782));
	}

	@Test
	@DisplayName("POST /worker-sensor/locations - 빈 리스트 요청")
	void getWorkersLocation_emptyListTest() throws Exception {
		// given
		List<Long> emptyWorkerIds = Collections.emptyList();
		
		given(workerSensorService.getWorkersLocation(emptyWorkerIds)).willReturn(Collections.emptyList());

		// when & then
		mockMvc.perform(post("/worker-sensor/locations")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(emptyWorkerIds)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("success"))
			.andExpect(jsonPath("$.data.length()").value(0));
	}

	@Test
	@DisplayName("POST /worker-sensor/locations - 일부 데이터만 존재")
	void getWorkersLocation_partialDataTest() throws Exception {
		// given
		List<Long> workerIds = Arrays.asList(1L, 2L, 3L);
		
		// 1L만 센서 데이터 존재
		List<WorkerLocationResponse> partialResponses = Arrays.asList(
			new WorkerLocationResponse(1L, 37.5665, 126.9780)
		);
		
		given(workerSensorService.getWorkersLocation(workerIds)).willReturn(partialResponses);

		// when & then
		mockMvc.perform(post("/worker-sensor/locations")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(workerIds)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("success"))
			.andExpect(jsonPath("$.data.length()").value(1))
			.andExpect(jsonPath("$.data[0].workerId").value(1L))
			.andExpect(jsonPath("$.data[0].latitude").value(37.5665))
			.andExpect(jsonPath("$.data[0].longitude").value(126.9780));
	}

	private byte[] createBinaryData(Double latitude, Double longitude, Double heartRate,
		Long steps, Double speed, Double pace, Long stepPerMinute) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		dos.writeDouble(latitude);
		dos.writeDouble(longitude);
		dos.writeDouble(heartRate);
		dos.writeLong(steps);
		dos.writeDouble(speed);
		dos.writeDouble(pace);
		dos.writeLong(stepPerMinute);

		return bos.toByteArray();
	}
}