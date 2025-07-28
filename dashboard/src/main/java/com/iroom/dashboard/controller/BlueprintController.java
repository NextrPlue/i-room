package com.iroom.dashboard.controller;

import com.iroom.dashboard.dto.request.BlueprintRequest;
import com.iroom.dashboard.dto.response.BlueprintResponse;
import com.iroom.dashboard.dto.response.PagedResponse;
import com.iroom.dashboard.service.BlueprintService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/blueprints")
@RequiredArgsConstructor
public class BlueprintController {

	private final BlueprintService blueprintService;

	// 도면 등록
	@PostMapping
	public ResponseEntity<BlueprintResponse> createBlueprint(@RequestBody BlueprintRequest request) {
		BlueprintResponse response = blueprintService.createBlueprint(request);
		return ResponseEntity.ok(response);
	}

	// 도면 수정
	@PutMapping("/{id}")
	public ResponseEntity<BlueprintResponse> updateBlueprint(@PathVariable Long id,
		@RequestBody BlueprintRequest request) {
		BlueprintResponse response = blueprintService.updateBlueprint(id, request);
		return ResponseEntity.ok(response);
	}

	// 도면 삭제
	@DeleteMapping("/{id}")
	public ResponseEntity<Map<String, Object>> deleteBlueprint(@PathVariable Long id) {
		blueprintService.deleteBlueprint(id);
		return ResponseEntity.ok(Map.of("message", "도면 삭제 완료", "deletedId", id));
	}

	// 도면 전체 조회
	@GetMapping
	public ResponseEntity<PagedResponse<BlueprintResponse>> getAllBlueprints(
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size
	) {
		if (size > 50)
			size = 50;
		if (size < 0)
			size = 0;

		PagedResponse<BlueprintResponse> responses = blueprintService.getAllBlueprints(page, size);
		return ResponseEntity.ok(responses);
	}
}
