package com.iroom.dashboard.dto.response;



import java.time.LocalDateTime;

public record DashBoardResponse(
        String metricType,
        Integer metricValue,
        LocalDateTime recordedAt
) {}