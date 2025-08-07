package com.iroom.sensor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iroom.sensor.dto.HeavyEquipment.*;
import com.iroom.sensor.service.HeavyEquipmentService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@WebMvcTest(controllers = HeavyEquipmentController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
public class HeavyEquipmentControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private HeavyEquipmentService equipmentService;

	@Test
	@DisplayName("POST /heavy-equipments/register - 장비 등록 성공 테스트")
	void registerTest() throws Exception {
		// given
		EquipmentRegisterRequest request = new EquipmentRegisterRequest("크레인A-3", "크레인", 15.0);
		EquipmentRegisterResponse response = new EquipmentRegisterResponse(1L, "크레인A-3", "크레인", 15.0);
		given(equipmentService.register(request)).willReturn(response);

		// when & then
		mockMvc.perform(post("/heavy-equipments/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("success"))
			.andExpect(jsonPath("$.data.id").value(1L))
			.andExpect(jsonPath("$.data.name").value("크레인A-3"))
			.andExpect(jsonPath("$.data.type").value("크레인"))
			.andExpect(jsonPath("$.data.radius").value(15.0));

	}

	@Test
	@DisplayName("POST /heavy-equipments/location - 장비 위치 업데이트 테스트 (binary 데이터)")
	void updateEquipmentLocationTest() throws Exception {
		// given
		Long equipmentId = 1L;
		Double latitude = 35.8343;
		Double longitude = 128.4723;

		byte[] binaryData = createBinaryData(equipmentId, latitude, longitude);

		EquipmentUpdateLocationResponse response = new EquipmentUpdateLocationResponse(
			equipmentId, latitude, longitude
		);

		given(equipmentService.updateLocation(binaryData)).willReturn(response);

		// when & then
		mockMvc.perform(post("/heavy-equipments/location")
				.contentType("application/octet-stream")
				.content(binaryData))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("success"))
			.andExpect(jsonPath("$.data.id").value(equipmentId))
			.andExpect(jsonPath("$.data.latitude").value(latitude))
			.andExpect(jsonPath("$.data.longitude").value(longitude));
	}

	private byte[] createBinaryData(Long equipmentId, Double latitude, Double longitude) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		dos.writeLong(equipmentId);
		dos.writeDouble(latitude);
		dos.writeDouble(longitude);

		return bos.toByteArray();
	}
}
