package com.iroom.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iroom.user.common.dto.request.LoginRequest;
import com.iroom.user.common.dto.response.LoginResponse;
import com.iroom.user.common.dto.response.PagedResponse;
import com.iroom.user.worker.controller.WorkerController;
import com.iroom.user.worker.dto.request.WorkerRegisterRequest;
import com.iroom.user.worker.dto.request.WorkerUpdateInfoRequest;
import com.iroom.user.worker.dto.request.WorkerUpdatePasswordRequest;
import com.iroom.user.worker.dto.response.WorkerInfoResponse;
import com.iroom.user.worker.dto.response.WorkerRegisterResponse;
import com.iroom.user.worker.dto.response.WorkerUpdateResponse;
import com.iroom.user.worker.enums.BloodType;
import com.iroom.user.worker.enums.Gender;
import com.iroom.user.worker.enums.WorkerRole;
import com.iroom.user.worker.service.WorkerService;

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

@WebMvcTest(value = WorkerController.class, excludeAutoConfiguration = {
	org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
public class WorkerControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private WorkerService workerService;

	@Test
	@DisplayName("POST /workers/register - 근로자 등록 성공")
	void registerSuccessTest() throws Exception {
		// given
		WorkerRegisterRequest request = new WorkerRegisterRequest(
			"김근로", "worker@example.com", "Worker123!", "010-1234-5678",
			BloodType.A, Gender.MALE, 30, 70.5f, 175.2f,
			"현장관리자", "건설업", "안전관리팀", "face-image-url.jpg"
		);
		WorkerRegisterResponse response = new WorkerRegisterResponse(
			1L, "김근로", "worker@example.com", "010-1234-5678",
			WorkerRole.WORKER, BloodType.A, Gender.MALE, 30, 70.5f, 175.2f,
			"현장관리자", "건설업", "안전관리팀", "face-image-url.jpg"
		);

		given(workerService.registerWorker(request)).willReturn(response);

		// when & then
		mockMvc.perform(post("/workers/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(1L))
			.andExpect(jsonPath("$.name").value("김근로"))
			.andExpect(jsonPath("$.email").value("worker@example.com"))
			.andExpect(jsonPath("$.phone").value("010-1234-5678"))
			.andExpect(jsonPath("$.role").value("WORKER"))
			.andExpect(jsonPath("$.gender").value("MALE"))
			.andExpect(jsonPath("$.bloodType").value("A"))
			.andExpect(jsonPath("$.age").value(30))
			.andExpect(jsonPath("$.weight").value(70.5))
			.andExpect(jsonPath("$.height").value(175.2))
			.andExpect(jsonPath("$.jobTitle").value("현장관리자"))
			.andExpect(jsonPath("$.occupation").value("건설업"))
			.andExpect(jsonPath("$.department").value("안전관리팀"));
	}

	@Test
	@DisplayName("POST /workers/register - 잘못된 요청 데이터")
	void registerValidationFailTest() throws Exception {
		// given
		WorkerRegisterRequest request = new WorkerRegisterRequest(
			"", "", "", "", null, null, null, null, null, null, null, null, null
		);

		// when & then
		mockMvc.perform(post("/workers/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("POST /workers/login - 로그인 성공")
	void loginSuccessTest() throws Exception {
		// given
		LoginRequest request = new LoginRequest("worker@example.com", "Worker123!");
		LoginResponse response = new LoginResponse("jwt-token-example");

		given(workerService.login(request)).willReturn(response);

		// when & then
		mockMvc.perform(post("/workers/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.token").value("jwt-token-example"));
	}

	@Test
	@DisplayName("POST /workers/login - 잘못된 요청 데이터")
	void loginValidationFailTest() throws Exception {
		// given
		LoginRequest request = new LoginRequest("", "");

		// when & then
		mockMvc.perform(post("/workers/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("PUT /workers/{workerId} - 근로자 정보 수정 성공")
	void updateInfoSuccessTest() throws Exception {
		// given
		Long workerId = 1L;
		WorkerUpdateInfoRequest request = new WorkerUpdateInfoRequest(
			"김수정", "updated@example.com", "010-9876-5432",
			BloodType.A, Gender.MALE, 30, 70.5f, 175.2f,
			"현장관리자", "건설업", "안전관리팀", "face-image-url.jpg"
		);
		WorkerUpdateResponse response = new WorkerUpdateResponse(
			1L, "김수정", "updated@example.com", "010-9876-5432",
			WorkerRole.WORKER, BloodType.A, Gender.MALE, 30, 70.5f, 175.2f,
			"현장관리자", "건설업", "안전관리팀", "face-image-url.jpg"
		);

		given(workerService.updateWorkerInfo(workerId, request)).willReturn(response);

		// when & then
		mockMvc.perform(put("/workers/{workerId}", workerId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(1L))
			.andExpect(jsonPath("$.name").value("김수정"))
			.andExpect(jsonPath("$.email").value("updated@example.com"))
			.andExpect(jsonPath("$.phone").value("010-9876-5432"));
	}

	@Test
	@DisplayName("PUT /workers/{workerId} - 잘못된 요청 데이터")
	void updateInfoValidationFailTest() throws Exception {
		// given
		Long workerId = 1L;
		WorkerUpdateInfoRequest request = new WorkerUpdateInfoRequest("", "", "", null, null, null, null, null, null,
			null, null, null);

		// when & then
		mockMvc.perform(put("/workers/{workerId}", workerId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("PUT /workers/password - 비밀번호 변경 성공")
	void updatePasswordSuccessTest() throws Exception {
		// given
		Long workerId = 1L;
		WorkerUpdatePasswordRequest request = new WorkerUpdatePasswordRequest("Current123!", "NewPass456@");

		doNothing().when(workerService).updateWorkerPassword(workerId, request);

		// when & then
		mockMvc.perform(put("/workers/password")
				.header("X-User-Id", "1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNoContent());
	}

	@Test
	@DisplayName("PUT /workers/password - X-User-Id 헤더 누락")
	void updatePasswordMissingHeaderTest() throws Exception {
		// given
		WorkerUpdatePasswordRequest request = new WorkerUpdatePasswordRequest("Current123!", "NewPass456@");

		// when & then
		mockMvc.perform(put("/workers/password")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("PUT /workers/password - 잘못된 요청 데이터")
	void updatePasswordValidationFailTest() throws Exception {
		// given
		Long workerId = 1L;
		WorkerUpdatePasswordRequest request = new WorkerUpdatePasswordRequest("", "");

		// when & then
		mockMvc.perform(put("/workers/password")
				.header("X-User-Id", "1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("GET /workers - 근로자 목록 조회 성공")
	void getWorkersSuccessTest() throws Exception {
		// given
		WorkerInfoResponse worker1 = new WorkerInfoResponse(
			1L, "김근로1", "worker1@example.com", "010-1111-1111",
			WorkerRole.WORKER, BloodType.A, Gender.MALE, 30, 70.5f, 175.2f,
			"현장관리자", "건설업", "안전관리팀", "face1.jpg",
			LocalDateTime.now(), LocalDateTime.now()
		);
		WorkerInfoResponse worker2 = new WorkerInfoResponse(
			2L, "김근로2", "worker2@example.com", "010-2222-2222",
			WorkerRole.WORKER, BloodType.B, Gender.FEMALE, 28, 60.0f, 165.5f,
			"안전관리자", "건설업", "안전관리팀", "face2.jpg",
			LocalDateTime.now(), LocalDateTime.now()
		);
		PagedResponse<WorkerInfoResponse> response = new PagedResponse<>(
			new PageImpl<>(List.of(worker1, worker2), PageRequest.of(0, 10), 2)
		);

		given(workerService.getWorkers(null, null, 0, 10)).willReturn(response);

		// when & then
		mockMvc.perform(get("/workers")
				.param("page", "0")
				.param("size", "10"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content").isArray())
			.andExpect(jsonPath("$.content[0].id").value(1L))
			.andExpect(jsonPath("$.content[0].name").value("김근로1"))
			.andExpect(jsonPath("$.content[1].id").value(2L))
			.andExpect(jsonPath("$.content[1].name").value("김근로2"))
			.andExpect(jsonPath("$.totalElements").value(2));
	}

	@Test
	@DisplayName("GET /workers - 검색 파라미터 포함 조회")
	void getWorkersWithSearchTest() throws Exception {
		// given
		WorkerInfoResponse worker1 = new WorkerInfoResponse(
			1L, "김근로", "worker@example.com", "010-1111-1111",
			WorkerRole.WORKER, BloodType.A, Gender.MALE, 30, 70.5f, 175.2f,
			"현장관리자", "건설업", "안전관리팀", "face.jpg",
			LocalDateTime.now(), LocalDateTime.now()
		);
		PagedResponse<WorkerInfoResponse> response = new PagedResponse<>(
			new PageImpl<>(List.of(worker1), PageRequest.of(0, 10), 1)
		);

		given(workerService.getWorkers("name", "김근로", 0, 10)).willReturn(response);

		// when & then
		mockMvc.perform(get("/workers")
				.param("target", "name")
				.param("keyword", "김근로")
				.param("page", "0")
				.param("size", "10"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content").isArray())
			.andExpect(jsonPath("$.content[0].name").value("김근로"))
			.andExpect(jsonPath("$.totalElements").value(1));
	}

	@Test
	@DisplayName("GET /workers - size 범위 검증 (최대 50)")
	void getWorkersSizeMaxLimitTest() throws Exception {
		// given
		PagedResponse<WorkerInfoResponse> response = new PagedResponse<>(
			new PageImpl<>(List.of(), PageRequest.of(0, 50), 0)
		);

		given(workerService.getWorkers(null, null, 0, 50)).willReturn(response);

		// when & then
		mockMvc.perform(get("/workers")
				.param("page", "0")
				.param("size", "100"))
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName("GET /workers/me - 내 정보 조회 성공")
	void getMyInfoSuccessTest() throws Exception {
		// given
		Long workerId = 1L;
		WorkerInfoResponse response = new WorkerInfoResponse(
			1L, "김근로", "worker@example.com", "010-1234-5678",
			WorkerRole.WORKER, BloodType.A, Gender.MALE, 30, 70.5f, 175.2f,
			"현장관리자", "건설업", "안전관리팀", "face.jpg",
			LocalDateTime.now(), LocalDateTime.now()
		);

		given(workerService.getWorkerInfo(workerId)).willReturn(response);

		// when & then
		mockMvc.perform(get("/workers/me")
				.header("X-User-Id", "1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(1L))
			.andExpect(jsonPath("$.name").value("김근로"))
			.andExpect(jsonPath("$.email").value("worker@example.com"))
			.andExpect(jsonPath("$.phone").value("010-1234-5678"))
			.andExpect(jsonPath("$.role").value("WORKER"))
			.andExpect(jsonPath("$.gender").value("MALE"))
			.andExpect(jsonPath("$.bloodType").value("A"))
			.andExpect(jsonPath("$.jobTitle").value("현장관리자"));
	}

	@Test
	@DisplayName("GET /workers/me - X-User-Id 헤더 누락")
	void getMyInfoMissingHeaderTest() throws Exception {
		// when & then
		mockMvc.perform(get("/workers/me"))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("GET /workers/{workerId} - 특정 근로자 조회 성공")
	void getWorkerByIdSuccessTest() throws Exception {
		// given
		Long workerId = 1L;
		WorkerInfoResponse response = new WorkerInfoResponse(
			1L, "김근로", "worker@example.com", "010-1234-5678",
			WorkerRole.WORKER, BloodType.A, Gender.MALE, 30, 70.5f, 175.2f,
			"현장관리자", "건설업", "안전관리팀", "face.jpg",
			LocalDateTime.now(), LocalDateTime.now()
		);

		given(workerService.getWorkerById(workerId)).willReturn(response);

		// when & then
		mockMvc.perform(get("/workers/{workerId}", workerId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(1L))
			.andExpect(jsonPath("$.name").value("김근로"))
			.andExpect(jsonPath("$.email").value("worker@example.com"))
			.andExpect(jsonPath("$.jobTitle").value("현장관리자"))
			.andExpect(jsonPath("$.department").value("안전관리팀"));
	}

	@Test
	@DisplayName("DELETE /workers/{workerId} - 근로자 삭제 성공")
	void deleteWorkerSuccessTest() throws Exception {
		// given
		Long workerId = 1L;

		doNothing().when(workerService).deleteWorker(workerId);

		// when & then
		mockMvc.perform(delete("/workers/{workerId}", workerId))
			.andExpect(status().isNoContent());
	}
}