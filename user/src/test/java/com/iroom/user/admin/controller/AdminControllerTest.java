package com.iroom.user.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iroom.user.admin.dto.request.AdminSignUpRequest;
import com.iroom.user.admin.dto.request.AdminUpdateInfoRequest;
import com.iroom.user.admin.dto.request.AdminUpdatePasswordRequest;
import com.iroom.user.admin.dto.request.AdminUpdateRoleRequest;
import com.iroom.user.admin.dto.response.AdminInfoResponse;
import com.iroom.user.admin.dto.response.AdminSignUpResponse;
import com.iroom.user.admin.dto.response.AdminUpdateResponse;
import com.iroom.user.common.dto.request.LoginRequest;
import com.iroom.user.common.dto.response.LoginResponse;
import com.iroom.modulecommon.dto.response.PagedResponse;
import com.iroom.user.admin.enums.AdminRole;
import com.iroom.user.admin.service.AdminService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = AdminController.class, excludeAutoConfiguration = {
	org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
public class AdminControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private AdminService adminService;

	@Test
	@DisplayName("POST /admins/signup - 관리자 회원가입 성공")
	void signUpSuccessTest() throws Exception {
		// given
		AdminSignUpRequest request = new AdminSignUpRequest("admin", "admin@example.com", "admin123!", "010-1234-5678");
		AdminSignUpResponse response = new AdminSignUpResponse("admin", "admin@example.com", "010-1234-5678",
			AdminRole.ADMIN);

		given(adminService.signUp(request)).willReturn(response);

		// when & then
		mockMvc.perform(post("/admins/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("admin"))
			.andExpect(jsonPath("$.email").value("admin@example.com"))
			.andExpect(jsonPath("$.phone").value("010-1234-5678"))
			.andExpect(jsonPath("$.role").value("ADMIN"));
	}

	@Test
	@DisplayName("POST /admins/signup - 잘못된 요청 데이터")
	void signUpValidationFailTest() throws Exception {
		// given
		AdminSignUpRequest request = new AdminSignUpRequest("", "", "", "");

		// when & then
		mockMvc.perform(post("/admins/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("POST /admins/login - 로그인 성공")
	void loginSuccessTest() throws Exception {
		// given
		LoginRequest request = new LoginRequest("admin@example.com", "password123");
		LoginResponse response = new LoginResponse("jwt-token-example");

		given(adminService.login(request)).willReturn(response);

		// when & then
		mockMvc.perform(post("/admins/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.token").value("jwt-token-example"));
	}

	@Test
	@DisplayName("POST /admins/login - 잘못된 요청 데이터")
	void loginValidationFailTest() throws Exception {
		// given
		LoginRequest request = new LoginRequest("", "");

		// when & then
		mockMvc.perform(post("/admins/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("PUT /admins/me - 내 정보 수정 성공")
	void updateMyInfoSuccessTest() throws Exception {
		// given
		Long adminId = 1L;
		AdminUpdateInfoRequest request = new AdminUpdateInfoRequest("updatedName", "updated@example.com",
			"010-9876-5432");
		AdminUpdateResponse response = new AdminUpdateResponse(1L, "updatedName", "updated@example.com",
			"010-9876-5432",
			AdminRole.ADMIN);

		given(adminService.updateAdminInfo(adminId, request)).willReturn(response);

		// when & then
		mockMvc.perform(put("/admins/me")
				.header("X-User-Id", adminId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(1L))
			.andExpect(jsonPath("$.name").value("updatedName"))
			.andExpect(jsonPath("$.email").value("updated@example.com"))
			.andExpect(jsonPath("$.phone").value("010-9876-5432"))
			.andExpect(jsonPath("$.role").value("ADMIN"));
	}

	@Test
	@DisplayName("PUT /admins/me - X-User-Id 헤더 누락")
	void updateMyInfoMissingHeaderTest() throws Exception {
		// given
		AdminUpdateInfoRequest request = new AdminUpdateInfoRequest("updatedName", "updated@example.com",
			"010-9876-5432");

		// when & then
		mockMvc.perform(put("/admins/me")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("PUT /admins/me - 잘못된 요청 데이터")
	void updateMyInfoValidationFailTest() throws Exception {
		// given
		Long adminId = 1L;
		AdminUpdateInfoRequest request = new AdminUpdateInfoRequest("", "", "");

		// when & then
		mockMvc.perform(put("/admins/me")
				.header("X-User-Id", adminId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("PUT /admins/password - 비밀번호 변경 성공")
	void updatePasswordSuccessTest() throws Exception {
		// given
		Long adminId = 1L;
		AdminUpdatePasswordRequest request = new AdminUpdatePasswordRequest("!admin123", "!newpass123");

		// when
		doNothing().when(adminService).updateAdminPassword(adminId, request);

		// then
		mockMvc.perform(put("/admins/password")
				.header("X-User-Id", adminId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNoContent());
	}

	@Test
	@DisplayName("PUT /admins/password - X-User-Id 헤더 누락")
	void updatePasswordMissingHeaderTest() throws Exception {
		// given
		AdminUpdatePasswordRequest request = new AdminUpdatePasswordRequest("!admin123", "!newpass123");

		// when & then
		mockMvc.perform(put("/admins/password")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("PUT /admins/password - 잘못된 요청 데이터")
	void updatePasswordValidationFailTest() throws Exception {
		// given
		Long adminId = 1L;
		AdminUpdatePasswordRequest request = new AdminUpdatePasswordRequest("", "");

		// when & then
		mockMvc.perform(put("/admins/password")
				.header("X-User-Id", adminId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("PUT /admins/{adminId}/role - 권한 변경 성공")
	void updateRoleSuccessTest() throws Exception {
		// given
		Long adminId = 1L;
		AdminUpdateRoleRequest request = new AdminUpdateRoleRequest(AdminRole.READER);
		AdminUpdateResponse response = new AdminUpdateResponse(1L, "admin", "admin@example.com", "010-1234-5678",
			AdminRole.READER);

		given(adminService.updateAdminRole(adminId, request)).willReturn(response);

		// when & then
		mockMvc.perform(put("/admins/{adminId}/role", adminId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(1L))
			.andExpect(jsonPath("$.role").value("READER"));
	}

	@Test
	@DisplayName("GET /admins - 관리자 목록 조회 성공")
	void getAdminsSuccessTest() throws Exception {
		// given
		AdminInfoResponse admin1 = new AdminInfoResponse(1L, "admin1", "admin1@example.com", "010-1111-1111",
			AdminRole.ADMIN, LocalDateTime.now(), LocalDateTime.now());
		AdminInfoResponse admin2 = new AdminInfoResponse(2L, "admin2", "admin2@example.com", "010-2222-2222",
			AdminRole.READER, LocalDateTime.now(), LocalDateTime.now());
		PagedResponse<AdminInfoResponse> response = new PagedResponse<>(
			new PageImpl<>(List.of(admin1, admin2), PageRequest.of(0, 10), 2));

		given(adminService.getAdmins(null, null, 0, 10)).willReturn(response);

		// when & then
		mockMvc.perform(get("/admins")
				.param("page", "0")
				.param("size", "10"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content").isArray())
			.andExpect(jsonPath("$.content[0].id").value(1L))
			.andExpect(jsonPath("$.content[0].name").value("admin1"))
			.andExpect(jsonPath("$.content[1].id").value(2L))
			.andExpect(jsonPath("$.content[1].name").value("admin2"))
			.andExpect(jsonPath("$.totalElements").value(2));
	}

	@Test
	@DisplayName("GET /admins - 검색 파라미터 포함 조회")
	void getAdminsWithSearchTest() throws Exception {
		// given
		AdminInfoResponse admin1 = new AdminInfoResponse(1L, "admin1", "admin1@example.com", "010-1111-1111",
			AdminRole.ADMIN, LocalDateTime.now(), LocalDateTime.now());
		PagedResponse<AdminInfoResponse> response = new PagedResponse<>(
			new PageImpl<>(List.of(admin1), PageRequest.of(0, 10), 1));

		given(adminService.getAdmins("name", "admin1", 0, 10)).willReturn(response);

		// when & then
		mockMvc.perform(get("/admins")
				.param("target", "name")
				.param("keyword", "admin1")
				.param("page", "0")
				.param("size", "10"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content").isArray())
			.andExpect(jsonPath("$.content[0].name").value("admin1"))
			.andExpect(jsonPath("$.totalElements").value(1));
	}

	@Test
	@DisplayName("GET /admins - size 범위 검증 (최대 50)")
	void getAdminsSizeMaxLimitTest() throws Exception {
		// given
		PagedResponse<AdminInfoResponse> response = new PagedResponse<>(
			new PageImpl<>(List.of(), PageRequest.of(0, 50), 0));

		given(adminService.getAdmins(null, null, 0, 50)).willReturn(response);

		// when & then
		mockMvc.perform(get("/admins")
				.param("page", "0")
				.param("size", "100"))
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName("GET /admins/me - 내 정보 조회 성공")
	void getMyInfoSuccessTest() throws Exception {
		// given
		Long adminId = 1L;
		AdminInfoResponse response = new AdminInfoResponse(1L, "admin", "admin@example.com", "010-1234-5678",
			AdminRole.ADMIN, LocalDateTime.now(), LocalDateTime.now());

		given(adminService.getAdminInfo(adminId)).willReturn(response);

		// when & then
		mockMvc.perform(get("/admins/me")
				.header("X-User-Id", adminId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(1L))
			.andExpect(jsonPath("$.name").value("admin"))
			.andExpect(jsonPath("$.email").value("admin@example.com"))
			.andExpect(jsonPath("$.phone").value("010-1234-5678"))
			.andExpect(jsonPath("$.role").value("ADMIN"));
	}

	@Test
	@DisplayName("GET /admins/me - X-User-Id 헤더 누락")
	void getMyInfoMissingHeaderTest() throws Exception {
		// when & then
		mockMvc.perform(get("/admins/me"))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("GET /admins/{adminId} - 특정 관리자 조회 성공")
	void getAdminByIdSuccessTest() throws Exception {
		// given
		Long adminId = 1L;
		AdminInfoResponse response = new AdminInfoResponse(1L, "admin", "admin@example.com", "010-1234-5678",
			AdminRole.ADMIN, LocalDateTime.now(), LocalDateTime.now());

		given(adminService.getAdminById(adminId)).willReturn(response);

		// when & then
		mockMvc.perform(get("/admins/{adminId}", adminId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(1L))
			.andExpect(jsonPath("$.name").value("admin"))
			.andExpect(jsonPath("$.email").value("admin@example.com"))
			.andExpect(jsonPath("$.role").value("ADMIN"));
	}

	@Test
	@DisplayName("DELETE /admins/{adminId} - 관리자 삭제 성공")
	void deleteAdminSuccessTest() throws Exception {
		// given
		Long adminId = 1L;

		doNothing().when(adminService).deleteAdmin(adminId);

		// when & then
		mockMvc.perform(delete("/admins/{adminId}", adminId))
			.andExpect(status().isNoContent());
	}
}