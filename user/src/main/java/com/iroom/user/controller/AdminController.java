package com.iroom.user.controller;

import com.iroom.user.dto.request.*;
import com.iroom.user.dto.response.*;
import com.iroom.user.service.AdminService;
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
    public ResponseEntity<AdminSignUpResponse> register(@Valid @RequestBody AdminSignUpRequest request) {
        AdminSignUpResponse response = adminService.signUp(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = adminService.login(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<AdminUpdateResponse> updateInfo(@RequestHeader("X-User-Id") Long id, @Valid @RequestBody AdminUpdateInfoRequest request) {
        AdminUpdateResponse response = adminService.updateAdminInfo(id, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/password")
    public ResponseEntity<Void> updatePassword(@RequestHeader("X-User-Id") Long id, @Valid @RequestBody AdminUpdatePasswordRequest request) {
        adminService.updateAdminPassword(id, request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{adminId}/role")
    public ResponseEntity<AdminUpdateResponse> updateRole(@PathVariable Long adminId, @RequestBody AdminUpdateRoleRequest request) {
        AdminUpdateResponse response = adminService.updateAdminRole(adminId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<AdminInfoResponse>> getAdmins(
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
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<AdminInfoResponse> getMyInfo(@RequestHeader("X-User-Id") Long id) {
        AdminInfoResponse response = adminService.getAdminInfo(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{adminId}")
    public ResponseEntity<Void> deleteAdmin(@PathVariable Long adminId) {
        adminService.deleteAdmin(adminId);
        return ResponseEntity.noContent().build();
    }
}
