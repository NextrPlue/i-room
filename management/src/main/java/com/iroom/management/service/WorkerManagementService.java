package com.iroom.management.service;

import com.iroom.management.dto.request.WorkerManagementRequest;
import com.iroom.management.dto.response.WorkerManagementResponse;

public interface WorkerManagementService {
    // 근로자 입장
    WorkerManagementResponse enterWorker(WorkerManagementRequest requestDto);
    // 근로자 퇴장
    WorkerManagementResponse exitWorker(Long workerId);
}
