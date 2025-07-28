package com.iroom.sensor.controller;

import com.iroom.sensor.dto.WorkerHealth.WorkerUpdateLocationRequest;
import com.iroom.sensor.dto.WorkerHealth.WorkerUpdateLocationResponse;
import com.iroom.sensor.dto.WorkerHealth.WorkerUpdateVitalSignsRequest;
import com.iroom.sensor.dto.WorkerHealth.WorkerUpdateVitalSignsResponse;
import com.iroom.sensor.service.WorkerHealthService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/worker-health")
@RequiredArgsConstructor
public class WorkerHealthController {

	private final WorkerHealthService workerHealthService;

	//위치 업데이트
	@PostMapping("/location")
	public ResponseEntity<WorkerUpdateLocationResponse> updateLocation(
		@RequestBody WorkerUpdateLocationRequest request
	) {
		WorkerUpdateLocationResponse response = workerHealthService.updateLocation(request);
		return ResponseEntity.ok(response);
	}

	//생체정보 업데이트
	@PostMapping("/vital-signs")
	public ResponseEntity<WorkerUpdateVitalSignsResponse> updateVitalSigns(
		@RequestBody WorkerUpdateVitalSignsRequest request
	) {
		WorkerUpdateVitalSignsResponse response = workerHealthService.updateVitalSigns(request);
		return ResponseEntity.ok(response);
	}

	//위치 정보 조회
	@GetMapping("/{workerId}/location")
	public ResponseEntity<WorkerUpdateLocationResponse> getWorkerLocation(
		@PathVariable Long workerId
	) {
		WorkerUpdateLocationResponse response = workerHealthService.getWorkerLocation(workerId);
		return ResponseEntity.ok(response);
	}
}
