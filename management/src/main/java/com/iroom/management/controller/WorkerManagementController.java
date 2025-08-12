package com.iroom.management.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iroom.management.dto.response.WorkerManagementResponse;
import com.iroom.management.dto.response.WorkerStatsResponse;
import com.iroom.management.service.WorkerManagementService;
import com.iroom.modulecommon.dto.response.ApiResponse;
import com.iroom.modulecommon.dto.response.PagedResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/entries")
@RequiredArgsConstructor
public class WorkerManagementController {
	private final WorkerManagementService workerManagementService;

	@PostMapping("/{workerId}/check-in")
	public ResponseEntity<ApiResponse<WorkerManagementResponse>> checkIn(@PathVariable Long workerId) {
		WorkerManagementResponse response = workerManagementService.enterWorker(workerId);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@PostMapping("/{workerId}/check-out")
	public ResponseEntity<ApiResponse<WorkerManagementResponse>> exit(@PathVariable Long workerId) {
		WorkerManagementResponse response = workerManagementService.exitWorker(workerId);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@GetMapping("/{workerId}")
	public ResponseEntity<ApiResponse<WorkerManagementResponse>> getEntry(@PathVariable Long workerId) {
		WorkerManagementResponse response = workerManagementService.getEntryByWorkerId(workerId);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<PagedResponse<WorkerManagementResponse>>> getEntries(
			@RequestParam(required = false) String date,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		if (size > 50) {
			size = 50;
		}
		
		if (size < 0) {
			size = 0;
		}
		
		PagedResponse<WorkerManagementResponse> response = workerManagementService.getEntries(date, page, size);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@GetMapping("/statistics")
	public ResponseEntity<ApiResponse<WorkerStatsResponse>> getWorkerStats() {
		WorkerStatsResponse response = workerManagementService.getWorkerStatistics();
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@GetMapping("/me")
	public ResponseEntity<ApiResponse<WorkerManagementResponse>> getMyInfo(@RequestHeader("X-User-Id") Long workerId) {
		WorkerManagementResponse response = workerManagementService.getWorkerEntry(workerId);
		return ResponseEntity.ok(ApiResponse.success(response));
	}
}
