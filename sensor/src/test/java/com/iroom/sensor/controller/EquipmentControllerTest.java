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

@WebMvcTest(controllers = HeavyEquipmentController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
public class EquipmentControllerTest {

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
			.andExpect(jsonPath("$.id").value(1L))
			.andExpect(jsonPath("$.name").value("크레인A-3"))
			.andExpect(jsonPath("$.type").value("크레인"))
			.andExpect(jsonPath("$.radius").value(15.0));

	}

	@Test
	@DisplayName("PUT /heavy-equipments/location - 위치 업데이트 테스트")
	void updateLocation() throws Exception {
		// given
		Long workerId = 1L;
		Double latitude = 35.8343;
		Double longitude = 128.4723;
		EquipmentUpdateLocationRequest request = new EquipmentUpdateLocationRequest(workerId, latitude, longitude);
		EquipmentUpdateLocationResponse response = new EquipmentUpdateLocationResponse(workerId, latitude, longitude);

		given(equipmentService.updateLocation(request)).willReturn(response);

		// when & then
		mockMvc.perform(put("/heavy-equipments/location")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(1L))
			.andExpect(jsonPath("$.latitude").value(latitude))
			.andExpect(jsonPath("$.longitude").value(longitude));
	}
}
