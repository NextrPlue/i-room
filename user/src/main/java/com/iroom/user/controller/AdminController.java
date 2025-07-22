package com.iroom.user.controller;

import com.iroom.user.dto.request.AdminSignUpRequest;
import com.iroom.user.dto.response.AdminSignUpResponse;
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
}
