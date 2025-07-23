package com.iroom.sensor.dto.WorkerHealth;

public record WorkerUpdateLocationResponse(
        Long workerId,
        String location
) {}
