package com.iroom.user.admin.controller;

import com.iroom.modulecommon.dto.response.ApiResponse;
import com.iroom.user.admin.dto.request.AdminSignUpRequest;
import com.iroom.user.admin.dto.request.AdminUpdateInfoRequest;
import com.iroom.user.admin.dto.request.AdminUpdatePasswordRequest;
import com.iroom.user.admin.dto.request.AdminUpdateRoleRequest;
import com.iroom.user.admin.dto.response.AdminInfoResponse;
import com.iroom.user.admin.dto.response.AdminSignUpResponse;
import com.iroom.user.admin.dto.response.AdminUpdateResponse;
import com.iroom.user.common.dto.request.LoginRequest;
import com.iroom.user.common.dto.response.LoginResponse;
import com.iroom.modulecommon.dto.response.PagedResponse;
import com.iroom.user.admin.service.AdminService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admins")
@RequiredArgsConstructor
public class AdminController {
	private final AdminService adminService;

	@PostMapping("/signup")
	public ResponseEntity<ApiResponse<AdminSignUpResponse>> register(@Valid @RequestBody AdminSignUpRequest request) {
		AdminSignUpResponse response = adminService.signUp(request);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
		LoginResponse response = adminService.login(request);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@PutMapping("/me")
	public ResponseEntity<ApiResponse<AdminUpdateResponse>> updateInfo(@RequestHeader("X-User-Id") Long id,
		@Valid @RequestBody AdminUpdateInfoRequest request) {
		AdminUpdateResponse response = adminService.updateAdminInfo(id, request);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@PutMapping("/password")
	public ResponseEntity<ApiResponse<String>> updatePassword(@RequestHeader("X-User-Id") Long id,
		@Valid @RequestBody AdminUpdatePasswordRequest request) {
		adminService.updateAdminPassword(id, request);
		return ResponseEntity.ok(ApiResponse.success("비밀번호가 성공적으로 변경되었습니다."));
	}

	@PutMapping("/{adminId}/role")
	public ResponseEntity<ApiResponse<AdminUpdateResponse>> updateRole(@PathVariable Long adminId,
		@RequestBody AdminUpdateRoleRequest request) {
		AdminUpdateResponse response = adminService.updateAdminRole(adminId, request);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<PagedResponse<AdminInfoResponse>>> getAdmins(
		@RequestParam(required = false) String target,
		@RequestParam(required = false) String keyword,
		@RequestParam(defaultValue = "0") Integer page,
		@RequestParam(defaultValue = "10") Integer size) {
		if (size > 50) {
			size = 50;
		}

		if (size < 0) {
			size = 0;
		}

		PagedResponse<AdminInfoResponse> response = adminService.getAdmins(target, keyword, page, size);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@GetMapping("/me")
	public ResponseEntity<ApiResponse<AdminInfoResponse>> getMyInfo(@RequestHeader("X-User-Id") Long id) {
		AdminInfoResponse response = adminService.getAdminInfo(id);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@GetMapping("/{adminId}")
	public ResponseEntity<ApiResponse<AdminInfoResponse>> getAdmin(@PathVariable Long adminId) {
		AdminInfoResponse response = adminService.getAdminById(adminId);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@DeleteMapping("/{adminId}")
	public ResponseEntity<ApiResponse<String>> deleteAdmin(@PathVariable Long adminId) {
		adminService.deleteAdmin(adminId);
		return ResponseEntity.ok(ApiResponse.success("관리자가 성공적으로 삭제되었습니다."));
	}
}
