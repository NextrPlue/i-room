package com.iroom.sensor.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.iroom.sensor.dto.WorkerSensor.WorkerSensorUpdateResponse;
import com.iroom.sensor.dto.WorkerSensor.WorkerLocationResponse;
import com.iroom.sensor.service.WorkerSensorService;

@WebMvcTest(controllers = WorkerSensorController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
public class WorkerSensorControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private WorkerSensorService workerSensorService;

	@Test
	@DisplayName("POST /worker-sensor/update - 통합 센서 데이터 업데이트 테스트 (모든 데이터)")
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
		mockMvc.perform(post("/worker-sensor/update")
				.header("X-User-Id", workerId)
				.contentType("application/octet-stream")
				.content(binaryData))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.workerId").value(workerId))
			.andExpect(jsonPath("$.latitude").value(latitude))
			.andExpect(jsonPath("$.longitude").value(longitude))
			.andExpect(jsonPath("$.heartRate").value(heartRate))
			.andExpect(jsonPath("$.steps").value(steps))
			.andExpect(jsonPath("$.speed").value(speed))
			.andExpect(jsonPath("$.pace").value(pace))
			.andExpect(jsonPath("$.stepPerMinute").value(stepPerMinute));
	}

	@Test
	@DisplayName("POST /worker-sensor/update - 통합 센서 데이터 업데이트 테스트 (위치만)")
	void updateWorkerSensorLocationOnlyTest() throws Exception {
		// given
		Long workerId = 2L;
		Double latitude = 35.8343;
		Double longitude = 128.4723;
		Double heartRate = 0.0;
		Long steps = 0L;
		Double speed = 0.0;
		Double pace = 0.0;
		Long stepPerMinute = 0L;

		byte[] binaryData = createBinaryData(latitude, longitude, heartRate, steps, speed, pace, stepPerMinute);
		WorkerSensorUpdateResponse response = new WorkerSensorUpdateResponse(workerId, latitude, longitude, heartRate,
			steps, speed, pace, stepPerMinute);
		given(workerSensorService.updateSensor(workerId, binaryData)).willReturn(response);

		// when & then
		mockMvc.perform(post("/worker-sensor/update")
				.header("X-User-Id", workerId)
				.contentType("application/octet-stream")
				.content(binaryData))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.workerId").value(workerId))
			.andExpect(jsonPath("$.latitude").value(latitude))
			.andExpect(jsonPath("$.longitude").value(longitude))
			.andExpect(jsonPath("$.heartRate").value(heartRate));
	}

	@Test
	@DisplayName("POST /worker-sensor/update - 통합 센서 데이터 업데이트 테스트 (심박수만)")
	void updateWorkerSensorHeartRateOnlyTest() throws Exception {
		// given
		Long workerId = 3L;
		Double latitude = 0.0;
		Double longitude = 0.0;
		Double heartRate = 90.0;
		Long steps = 0L;
		Double speed = 0.0;
		Double pace = 0.0;
		Long stepPerMinute = 0L;

		byte[] binaryData = createBinaryData(latitude, longitude, heartRate, steps, speed, pace, stepPerMinute);
		WorkerSensorUpdateResponse response = new WorkerSensorUpdateResponse(workerId, latitude, longitude, heartRate,
			steps, speed, pace, stepPerMinute);
		given(workerSensorService.updateSensor(workerId, binaryData)).willReturn(response);

		// when & then
		mockMvc.perform(post("/worker-sensor/update")
				.header("X-User-Id", workerId)
				.contentType("application/octet-stream")
				.content(binaryData))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.workerId").value(workerId))
			.andExpect(jsonPath("$.latitude").value(latitude))
			.andExpect(jsonPath("$.longitude").value(longitude))
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