package com.iroom.management.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iroom.management.dto.response.WorkerManagementResponse;
import com.iroom.management.service.WorkerManagementService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/entries")
@RequiredArgsConstructor
public class WorkerManagementController {
	private final WorkerManagementService workerManagementService;

	@PostMapping("/{workerId}/check-in")
	public ResponseEntity<WorkerManagementResponse> checkIn(@PathVariable Long workerId) {
		WorkerManagementResponse response = workerManagementService.enterWorker(workerId);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/{workerId}/check-out")
	public ResponseEntity<WorkerManagementResponse> exit(@PathVariable Long workerId) {
		WorkerManagementResponse response = workerManagementService.exitWorker(workerId);
		return ResponseEntity.ok(response);
	}
}
