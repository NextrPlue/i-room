package com.iroom.management.dto.request;

import com.iroom.management.entity.WorkerManagement;

import java.time.LocalDateTime;


public record WorkerManagementRequest(
        Long id,
        Long workerId,
        LocalDateTime enterDate,
        LocalDateTime outDate
) {
    public WorkerManagement toEntity() {
        return WorkerManagement.builder()
                .workerId(workerId)
                .build();
    }
}

