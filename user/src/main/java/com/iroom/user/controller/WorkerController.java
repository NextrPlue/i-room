package com.iroom.user.controller;

import com.iroom.user.dto.request.LoginRequest;
import com.iroom.user.dto.request.WorkerRegisterRequest;
import com.iroom.user.dto.request.WorkerUpdateInfoRequest;
import com.iroom.user.dto.request.WorkerUpdatePasswordRequest;
import com.iroom.user.dto.response.*;
import com.iroom.user.service.WorkerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PutMapping("/{workerId}")
    public ResponseEntity<WorkerUpdateResponse> updateInfo(@PathVariable Long workerId, @Valid @RequestBody WorkerUpdateInfoRequest request) {
        WorkerUpdateResponse response = workerService.updateWorkerInfo(workerId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/password")
    public ResponseEntity<Void> updatePassword(@RequestHeader("X-User-Id") Long id, @Valid @RequestBody WorkerUpdatePasswordRequest request) {
        workerService.updateWorkerPassword(id, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<PagedResponse<WorkerInfoResponse>> getWorkers(
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

        PagedResponse<WorkerInfoResponse> response = workerService.getWorkers(target, keyword, page, size);
        return ResponseEntity.ok(response);
    }
}
