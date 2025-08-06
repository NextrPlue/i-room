package com.iroom.sensor.controller;

import com.iroom.sensor.dto.WorkerSensor.WorkerLocationResponse;
import com.iroom.sensor.dto.WorkerSensor.WorkerSensorUpdateResponse;
import com.iroom.sensor.service.WorkerSensorService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/worker-sensor")
@RequiredArgsConstructor
public class WorkerSensorController {

	private final WorkerSensorService workerSensorService;

	@PostMapping(value = "/update", consumes = "application/octet-stream")
	public ResponseEntity<WorkerSensorUpdateResponse> updateWorkerSensor(
		@RequestHeader("X-User-Id") Long workerId,
		@RequestBody byte[] binaryData
	) {
		WorkerSensorUpdateResponse response = workerSensorService.updateSensor(workerId, binaryData);
		return ResponseEntity.ok(response);
	}

	//위치 정보 조회
	@GetMapping("/{workerId}/location")
	public ResponseEntity<WorkerLocationResponse> getWorkerLocation(
		@PathVariable Long workerId
	) {
		WorkerLocationResponse response = workerSensorService.getWorkerLocation(workerId);
		return ResponseEntity.ok(response);
	}
}
