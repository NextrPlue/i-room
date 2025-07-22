package com.iroom.management.controller;

import com.iroom.management.dto.request.WorkerEduRequest;
import com.iroom.management.dto.response.WorkerEduResponse;
import com.iroom.management.service.WorkerEduService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("workerEdu")
@RequiredArgsConstructor
public class WorkerEduController {
    private final WorkerEduService workerEduService;

    // 안전교육 이수 등록
    @PostMapping("/record")
    public WorkerEduResponse recordEdu(@RequestBody WorkerEduRequest requestDto) {
        return workerEduService.recordEdu(requestDto);
    }

    // 교육 정보 조회
    @GetMapping("/{workerId}")
    public WorkerEduResponse getEduInfo(@PathVariable Long workerId) {
        return workerEduService.getEduInfo(workerId);
    }

}
