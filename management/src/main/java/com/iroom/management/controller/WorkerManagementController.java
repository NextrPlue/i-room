package com.iroom.management.controller;

import com.iroom.management.dto.request.WorkerManagementRequest;
import com.iroom.management.dto.response.WorkerManagementResponse;
import com.iroom.management.service.WorkerManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/workerManagement")
@RequiredArgsConstructor
public class WorkerManagementController {
    private final WorkerManagementService workerManagementService;

    @PostMapping("/enter")
    public ResponseEntity<WorkerManagementResponse> enter(@RequestBody WorkerManagementRequest requestDto) {
        WorkerManagementResponse response = workerManagementService.enterWorker(requestDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/exit/{workerId}")
    public ResponseEntity<WorkerManagementResponse> exit(@PathVariable Long workerId) {
        WorkerManagementResponse response = workerManagementService.exitWorker(workerId);
        return ResponseEntity.ok(response);
    }
}
