package com.iroom.user.controller;

import com.iroom.user.dto.request.*;
import com.iroom.user.dto.response.AdminSignUpResponse;
import com.iroom.user.dto.response.AdminUpdateResponse;
import com.iroom.user.dto.response.LoginResponse;
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
}
