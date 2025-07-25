package com.iroom.sensor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iroom.sensor.dto.HeavyEquipment.EquipmentRegisterRequest;
import com.iroom.sensor.dto.HeavyEquipment.EquipmentRegisterResponse;
import com.iroom.sensor.dto.HeavyEquipment.EquipmentUpdateLocationRequest;
import com.iroom.sensor.dto.HeavyEquipment.EquipmentUpdateLocationResponse;
import com.iroom.sensor.service.HeavyEquipmentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HeavyEquipmentController.class)
public class EquipmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private HeavyEquipmentService equipmentService;

    @Test
    @DisplayName("POST /heavy-equipments/register - 장비 등록 성공 테스트")
    void registerTest() throws Exception{
        EquipmentRegisterRequest request = new EquipmentRegisterRequest("크레인A-3", "크레인", 15.0);
        EquipmentRegisterResponse response = new EquipmentRegisterResponse(1L, "크레인A-3", "크레인", 15.0);

        given(equipmentService.register(request)).willReturn(response);

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
    void updateLocation_Success() throws Exception {
        // given
        EquipmentUpdateLocationRequest request = new EquipmentUpdateLocationRequest(1L, "354.8343, 128.4723");
        EquipmentUpdateLocationResponse response = new EquipmentUpdateLocationResponse(1L, "354.8343, 128.4723");

        given(equipmentService.updateLocation(request)).willReturn(response);

        // when & then
        mockMvc.perform(put("/heavy-equipments/location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.location").value("354.8343, 128.4723"));
    }
}
