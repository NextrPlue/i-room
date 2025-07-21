package com.iroom.admin.controller;

import com.iroom.admin.dto.request.SignUpRequest;
import com.iroom.admin.dto.response.SignUpResponse;
import com.iroom.admin.service.AdminService;
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
    public ResponseEntity<SignUpResponse> register(@RequestBody SignUpRequest request) {
        SignUpResponse response = adminService.signUp(request);
        return ResponseEntity.ok(response);
    }
}
