package com.iroom.management.controller;

import com.iroom.management.dto.request.WorkerManagementRequestDto;
import com.iroom.management.dto.response.WorkerManagementResponseDto;
import com.iroom.management.service.WorkerManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("workerManagement")
@RequiredArgsConstructor
public class WorkerManagementController {
    private final WorkerManagementService workerManagementService;

    @PostMapping("/enter")
    public ResponseEntity<WorkerManagementResponseDto> enter(@RequestBody WorkerManagementRequestDto requestDto) {
        WorkerManagementResponseDto response = workerManagementService.enterWorker(requestDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/exit/{workerId}")
    public ResponseEntity<WorkerManagementResponseDto> exit(@PathVariable Long workerId) {
        WorkerManagementResponseDto response = workerManagementService.exitWorker(workerId);
        return ResponseEntity.ok(response);
    }
}
