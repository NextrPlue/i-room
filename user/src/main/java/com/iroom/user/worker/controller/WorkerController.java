package com.iroom.user.worker.controller;

import com.iroom.modulecommon.dto.response.ApiResponse;
import com.iroom.user.common.dto.request.LoginRequest;
import com.iroom.user.common.dto.response.LoginResponse;
import com.iroom.modulecommon.dto.response.PagedResponse;
import com.iroom.user.worker.dto.request.WorkerRegisterRequest;
import com.iroom.user.worker.dto.request.WorkerUpdateInfoRequest;
import com.iroom.user.worker.dto.request.WorkerUpdatePasswordRequest;
import com.iroom.user.worker.dto.response.WorkerInfoResponse;
import com.iroom.user.worker.dto.response.WorkerRegisterResponse;
import com.iroom.user.worker.dto.response.WorkerUpdateResponse;
import com.iroom.user.worker.service.WorkerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/workers")
@RequiredArgsConstructor
public class WorkerController {

	private final WorkerService workerService;

	@PostMapping("/register")
	public ResponseEntity<ApiResponse<WorkerRegisterResponse>> register(
		@Valid @RequestBody WorkerRegisterRequest request) {
		WorkerRegisterResponse response = workerService.registerWorker(request);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
		LoginResponse response = workerService.login(request);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@PutMapping("/{workerId}")
	public ResponseEntity<ApiResponse<WorkerUpdateResponse>> updateInfo(@PathVariable Long workerId,
		@Valid @RequestBody WorkerUpdateInfoRequest request) {
		WorkerUpdateResponse response = workerService.updateWorkerInfo(workerId, request);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@PutMapping("/password")
	public ResponseEntity<ApiResponse<String>> updatePassword(@RequestHeader("X-User-Id") Long id,
		@Valid @RequestBody WorkerUpdatePasswordRequest request) {
		workerService.updateWorkerPassword(id, request);
		return ResponseEntity.ok(ApiResponse.success("비밀번호가 성공적으로 변경되었습니다."));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<PagedResponse<WorkerInfoResponse>>> getWorkers(
		@RequestParam(required = false) String target,
		@RequestParam(required = false) String keyword,
		@RequestParam(defaultValue = "0") Integer page,
		@RequestParam(defaultValue = "10") Integer size) {
		if (size > 50) {
			size = 50;
		}

		if (size < 0) {
			size = 0;
		}

		PagedResponse<WorkerInfoResponse> response = workerService.getWorkers(target, keyword, page, size);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@GetMapping("/me")
	public ResponseEntity<ApiResponse<WorkerInfoResponse>> getMyInfo(@RequestHeader("X-User-Id") Long id) {
		WorkerInfoResponse response = workerService.getWorkerInfo(id);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@GetMapping("/{workerId}")
	public ResponseEntity<ApiResponse<WorkerInfoResponse>> getWorker(@PathVariable Long workerId) {
		WorkerInfoResponse response = workerService.getWorkerById(workerId);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@DeleteMapping("/{workerId}")
	public ResponseEntity<ApiResponse<String>> deleteWorker(@PathVariable Long workerId) {
		workerService.deleteWorker(workerId);
		return ResponseEntity.ok(ApiResponse.success("근로자가 성공적으로 삭제되었습니다."));
	}
}
