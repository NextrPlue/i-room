package com.iroom.sensor.dto.WorkerHealth;

import com.iroom.sensor.entity.WorkerHealth;

public record WorkerUpdateLocationResponse(
        Long workerId,
        String location
) {
    // 엔티티 → DTO 변환 생성자
    public WorkerUpdateLocationResponse(WorkerHealth entity) {
        this(
                entity.getWorkerId(),
                entity.getWorkerLocation()
        );
    }
}
