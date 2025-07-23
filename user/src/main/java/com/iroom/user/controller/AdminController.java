package com.iroom.user.controller;

import com.iroom.user.dto.request.AdminSignUpRequest;
import com.iroom.user.dto.request.LoginRequest;
import com.iroom.user.dto.response.AdminSignUpResponse;
import com.iroom.user.dto.response.LoginResponse;
import com.iroom.user.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
