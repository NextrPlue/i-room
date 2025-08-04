package com.iroom.sensor.controller;

import com.iroom.sensor.dto.WorkerSensor.WorkerUpdateLocationRequest;
import com.iroom.sensor.dto.WorkerSensor.WorkerUpdateLocationResponse;
import com.iroom.sensor.dto.WorkerSensor.WorkerUpdateVitalSignsRequest;
import com.iroom.sensor.dto.WorkerSensor.WorkerUpdateVitalSignsResponse;
import com.iroom.sensor.service.WorkerSensorService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/worker-sensor")
@RequiredArgsConstructor
public class WorkerSensorController {

	private final WorkerSensorService workerSensorService;

	//위치 업데이트
	@PostMapping("/location")
	public ResponseEntity<WorkerUpdateLocationResponse> updateLocation(
		@RequestBody WorkerUpdateLocationRequest request
	) {
		WorkerUpdateLocationResponse response = workerSensorService.updateLocation(request);
		return ResponseEntity.ok(response);
	}

	//생체정보 업데이트
	@PostMapping("/vital-signs")
	public ResponseEntity<WorkerUpdateVitalSignsResponse> updateVitalSigns(
		@RequestBody WorkerUpdateVitalSignsRequest request
	) {
		WorkerUpdateVitalSignsResponse response = workerSensorService.updateVitalSigns(request);
		return ResponseEntity.ok(response);
	}

	//위치 정보 조회
	@GetMapping("/{workerId}/location")
	public ResponseEntity<WorkerUpdateLocationResponse> getWorkerLocation(
		@PathVariable Long workerId
	) {
		WorkerUpdateLocationResponse response = workerSensorService.getWorkerLocation(workerId);
		return ResponseEntity.ok(response);
	}
}
