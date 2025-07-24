package com.iroom.user.controller;

import com.iroom.user.dto.request.LoginRequest;
import com.iroom.user.dto.request.WorkerRegisterRequest;
import com.iroom.user.dto.response.LoginResponse;
import com.iroom.user.dto.response.WorkerRegisterResponse;
import com.iroom.user.service.WorkerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/workers")
@RequiredArgsConstructor
public class WorkerController {

    private final WorkerService workerService;

    @PostMapping("/register")
    public ResponseEntity<WorkerRegisterResponse> register(@Valid @RequestBody WorkerRegisterRequest request) {
        WorkerRegisterResponse response = workerService.registerWorker(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = workerService.login(request);
        return ResponseEntity.ok(response);
    }
}
