package com.iroom.management.dto.response;

import com.iroom.management.entity.WorkerEdu;
import com.iroom.management.entity.WorkerManagement;

import java.time.LocalDateTime;


public record WorkerManagementResponse (
        Long id,
        Long workerId,
        LocalDateTime enterDate,
        LocalDateTime outDate
){
    public WorkerManagementResponse(WorkerManagement entity) {
        this(
                entity.getId(),
                entity.getWorkerId(),
                entity.getEnterDate(),
                entity.getOutDate()
        );
    }
}
