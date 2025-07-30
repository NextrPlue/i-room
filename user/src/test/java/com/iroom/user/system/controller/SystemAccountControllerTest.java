package com.iroom.user.system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iroom.user.system.dto.request.SystemAuthRequest;
import com.iroom.user.system.dto.response.SystemAuthResponse;
import com.iroom.user.system.service.SystemAccountService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = SystemAccountController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
public class SystemAccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SystemAccountService systemAccountService;

    @Test
    @DisplayName("시스템 인증 API 성공")
    void authenticateApiTest() throws Exception {
        // given
        SystemAuthRequest request = new SystemAuthRequest("test-api-key");
        SystemAuthResponse response = new SystemAuthResponse("jwt-system-token");

        given(systemAccountService.authenticate(any(SystemAuthRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/systems/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("jwt-system-token"));
    }

    @Test
    @DisplayName("시스템 인증 API 실패 - 잘못된 요청 데이터")
    void authenticateApiFailInvalidApiKey() throws Exception {
        // given
        SystemAuthRequest request = new SystemAuthRequest("");

        // when & then
        mockMvc.perform(post("/systems/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }
}