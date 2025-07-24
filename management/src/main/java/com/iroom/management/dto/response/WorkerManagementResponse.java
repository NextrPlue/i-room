package com.iroom.management.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.iroom.management.entity.WorkerManagement;

import java.time.LocalDateTime;


public record WorkerManagementResponse (
        Long id,
        Long workerId,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime enterDate,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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
