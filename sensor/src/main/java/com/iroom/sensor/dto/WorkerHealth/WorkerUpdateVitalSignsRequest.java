package com.iroom.sensor.dto.WorkerHealth;

public record WorkerUpdateVitalSignsRequest(
        Long workerId,
        Integer heartRate,
        Float bodyTemperature
) {}
