package com.iroom.dashboard.danger.controller;

import com.iroom.dashboard.danger.dto.request.DangerAreaRequest;
import com.iroom.dashboard.danger.dto.response.DangerAreaResponse;
import com.iroom.dashboard.danger.service.DangerAreaService;
import com.iroom.modulecommon.dto.response.ApiResponse;
import com.iroom.modulecommon.dto.response.PagedResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/danger-areas")
@RequiredArgsConstructor
public class DangerAreaController {
	private final DangerAreaService dangerAreaService;

	@PostMapping
	public ResponseEntity<ApiResponse<DangerAreaResponse>> createDangerArea(@RequestBody DangerAreaRequest request) {
		DangerAreaResponse response = dangerAreaService.createDangerArea(request);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<DangerAreaResponse>> updateDangerArea(@PathVariable Long id,
		@RequestBody DangerAreaRequest request) {
		DangerAreaResponse response = dangerAreaService.updateDangerArea(id, request);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Map<String, Object>>> deleteDangerArea(@PathVariable Long id) {
		dangerAreaService.deleteDangerArea(id);
		Map<String, Object> result = Map.of("message", "위험구역 삭제 완료", "deletedId", id);
		return ResponseEntity.ok(ApiResponse.success(result));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<PagedResponse<DangerAreaResponse>>> getAllDangerAreas(
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size
	) {
		if (size > 50)
			size = 50;
		if (size < 0)
			size = 0;

		PagedResponse<DangerAreaResponse> response = dangerAreaService.getAllDangerAreas(page, size);
		return ResponseEntity.ok(ApiResponse.success(response));
	}
}
