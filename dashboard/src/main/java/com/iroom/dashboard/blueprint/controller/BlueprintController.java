package com.iroom.dashboard.blueprint.controller;

import com.iroom.dashboard.blueprint.dto.request.BlueprintRequest;
import com.iroom.dashboard.blueprint.dto.response.BlueprintResponse;
import com.iroom.dashboard.blueprint.service.BlueprintService;
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
	@PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ApiResponse<BlueprintResponse>> updateBlueprint(
		@PathVariable Long id,
		@RequestPart("data") BlueprintRequest request,
		@RequestPart(value = "file", required = false) MultipartFile file) {
			
		BlueprintResponse response = blueprintService.updateBlueprint(id, request, file);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	// 도면 삭제
	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Map<String, Object>>> deleteBlueprint(@PathVariable Long id) {
		blueprintService.deleteBlueprint(id);
		Map<String, Object> result = Map.of("message", "도면 삭제 완료", "deletedId", id);
		return ResponseEntity.ok(ApiResponse.success(result));
	}

	// 도면 단일 조회
	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<BlueprintResponse>> getBlueprint(@PathVariable Long id) {
		BlueprintResponse response = blueprintService.getBlueprint(id);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	// 도면 전체 조회
	@GetMapping
	public ResponseEntity<ApiResponse<PagedResponse<BlueprintResponse>>> getAllBlueprints(
		@RequestParam(required = false) String target,
		@RequestParam(required = false) String keyword,
		@RequestParam(defaultValue = "0") Integer page,
		@RequestParam(defaultValue = "10") Integer size
	) {
		if (size > 50) {
			size = 50;
		}

		if (size < 0) {
			size = 0;
		}

		PagedResponse<BlueprintResponse> responses = blueprintService.getAllBlueprints(target, keyword, page, size);
		return ResponseEntity.ok(ApiResponse.success(responses));
	}

	// 도면 이미지 조회
	@GetMapping("/{id}/image")
	public ResponseEntity<Resource> getBlueprintImage(@PathVariable Long id) {
		Resource resource = blueprintService.getBlueprintImageResource(id);
		return ResponseEntity.ok()
			.header("Content-Disposition", "inline")
			.contentType(MediaType.IMAGE_JPEG)
			.body(resource);
	}
}
