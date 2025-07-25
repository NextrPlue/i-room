package com.iroom.management.controller;

import com.iroom.management.dto.request.WorkerEduRequest;
import com.iroom.management.dto.response.WorkerEduResponse;
import com.iroom.management.service.WorkerEduService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/worker-education")
@RequiredArgsConstructor
public class WorkerEduController {
    private final WorkerEduService workerEduService;

    // 안전교육 이수 등록
    @PostMapping
    public ResponseEntity<WorkerEduResponse> recordEdu(@RequestBody WorkerEduRequest requestDto) {
        WorkerEduResponse response = workerEduService.recordEdu(requestDto);
        return ResponseEntity.ok(response);
    }

    // 교육 정보 조회
    @GetMapping("/workers/{workerId}")
    public ResponseEntity<List<WorkerEduResponse>> getEduInfo(@PathVariable Long workerId) {
        List<WorkerEduResponse> response = workerEduService.getEduInfo(workerId);
        return ResponseEntity.ok(response);
    }

}
