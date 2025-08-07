package com.iroom.dashboard.controller;

import com.iroom.dashboard.dto.request.BlueprintRequest;
import com.iroom.dashboard.dto.response.BlueprintResponse;
import com.iroom.dashboard.service.BlueprintService;
import com.iroom.modulecommon.dto.response.ApiResponse;
import com.iroom.modulecommon.dto.response.PagedResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/blueprints")
@RequiredArgsConstructor
public class BlueprintController {

	private final BlueprintService blueprintService;

	// 도면 등록
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ApiResponse<BlueprintResponse>> createBlueprint(
		@RequestPart("file") MultipartFile file,
		@RequestPart("data") BlueprintRequest blueprintUrl) {
		BlueprintResponse response = blueprintService.createBlueprint(file, blueprintUrl);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	// 도면 수정
	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<BlueprintResponse>> updateBlueprint(@PathVariable Long id,
		@RequestBody BlueprintRequest request) {
		BlueprintResponse response = blueprintService.updateBlueprint(id, request);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	// 도면 삭제
	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Map<String, Object>>> deleteBlueprint(@PathVariable Long id) {
		blueprintService.deleteBlueprint(id);
		Map<String, Object> result = Map.of("message", "도면 삭제 완료", "deletedId", id);
		return ResponseEntity.ok(ApiResponse.success(result));
	}

	// 도면 전체 조회
	@GetMapping
	public ResponseEntity<ApiResponse<PagedResponse<BlueprintResponse>>> getAllBlueprints(
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size
	) {
		if (size > 50)
			size = 50;
		if (size < 0)
			size = 0;

		PagedResponse<BlueprintResponse> responses = blueprintService.getAllBlueprints(page, size);
		return ResponseEntity.ok(ApiResponse.success(responses));
	}

	// 도면 이미지 조회
	@GetMapping("/{id}/image")
	public ResponseEntity<Resource> getBlueprintImage(@PathVariable Long id) {
		return blueprintService.getBlueprintImage(id);
	}
}
