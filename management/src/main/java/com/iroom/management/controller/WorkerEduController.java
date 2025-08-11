package com.iroom.management.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iroom.modulecommon.dto.response.ApiResponse;
import com.iroom.modulecommon.dto.response.PagedResponse;
import com.iroom.management.dto.request.WorkerEduRequest;
import com.iroom.management.dto.response.WorkerEduResponse;
import com.iroom.management.service.WorkerEduService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/worker-education")
@RequiredArgsConstructor
public class WorkerEduController {
	private final WorkerEduService workerEduService;

	// 안전교육 이수 등록
	@PostMapping
	public ResponseEntity<ApiResponse<WorkerEduResponse>> recordEdu(@RequestBody WorkerEduRequest requestDto) {
		WorkerEduResponse response = workerEduService.recordEdu(requestDto);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	// 교육 정보 조회
	// 로그인 정보가 근로자일 경우, 토큰에서 workerId 넘겨주도록 수정 필요
	@GetMapping("/workers/{workerId}")
	public ResponseEntity<ApiResponse<PagedResponse<WorkerEduResponse>>> getEduInfo(
		@PathVariable Long workerId,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size) {

		if (size > 50)
			size = 50;
		if (size < 0)
			size = 0;

		PagedResponse<WorkerEduResponse> response = workerEduService.getEduInfo(workerId, page, size);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@GetMapping("/me")
	public ResponseEntity<ApiResponse<PagedResponse<WorkerEduResponse>>> getMyEducation(
		@RequestHeader("X-User-Id") Long workerId,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size) {
		PagedResponse<WorkerEduResponse> response = workerEduService.getWorkerEdu(workerId, page, size);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

}
