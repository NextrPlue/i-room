package com.iroom.user.controller;

import com.iroom.user.dto.request.AdminSignUpRequest;
import com.iroom.user.dto.request.AdminUpdateInfoRequest;
import com.iroom.user.dto.request.LoginRequest;
import com.iroom.user.dto.response.AdminSignUpResponse;
import com.iroom.user.dto.response.AdminUpdateResponse;
import com.iroom.user.dto.response.LoginResponse;
import com.iroom.user.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @PostMapping("/signup")
    public ResponseEntity<AdminSignUpResponse> register(@RequestBody AdminSignUpRequest request) {
        AdminSignUpResponse response = adminService.signUp(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = adminService.login(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<AdminUpdateResponse> updateInfo(
            @RequestHeader("X-User-Id") Long id,
            @RequestBody AdminUpdateInfoRequest request) {
        AdminUpdateResponse response = adminService.updateAdminInfo(id, request);
        return ResponseEntity.ok(response);
    }
}
