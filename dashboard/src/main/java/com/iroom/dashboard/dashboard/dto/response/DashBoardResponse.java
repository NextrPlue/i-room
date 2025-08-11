package com.iroom.dashboard.dashboard.dto.response;



import java.time.LocalDate;


public record DashBoardResponse(
        String metricType,
        Integer metricValue,
        LocalDate recordedAt
) {}