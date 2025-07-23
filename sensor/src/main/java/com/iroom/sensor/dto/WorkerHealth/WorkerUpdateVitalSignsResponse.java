package com.iroom.sensor.dto.WorkerHealth;

public record WorkerUpdateVitalSignsResponse(
        Long workerId,
        Integer heartRate,
        Float bodyTemperature
) {}
