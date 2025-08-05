package com.iroom.sensor.controller;

import com.iroom.sensor.dto.WorkerSensor.WorkerLocationResponse;
import com.iroom.sensor.dto.WorkerSensor.WorkerSensorUpdateRequest;
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

	@PostMapping("/update")
	public ResponseEntity<WorkerSensorUpdateResponse> updateWorkerSensor(
		@RequestBody WorkerSensorUpdateRequest request
	) {
		WorkerSensorUpdateResponse response = workerSensorService.updateSensor(request);
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
