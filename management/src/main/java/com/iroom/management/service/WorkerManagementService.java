package com.iroom.management.service;

import com.iroom.management.dto.request.WorkerManagementRequestDto;
import com.iroom.management.dto.response.WorkerManagementResponseDto;

public interface WorkerManagementService {
    // 근로자 입장
    WorkerManagementResponseDto enterWorker(WorkerManagementRequestDto requestDto);
    // 근로자 퇴장
    WorkerManagementResponseDto exitWorker(Long workerId);
}
