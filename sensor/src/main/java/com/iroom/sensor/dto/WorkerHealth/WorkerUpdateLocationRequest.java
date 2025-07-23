package com.iroom.sensor.dto.WorkerHealth;

public record WorkerUpdateLocationRequest(
        Long workerId,
        String location
) {}
