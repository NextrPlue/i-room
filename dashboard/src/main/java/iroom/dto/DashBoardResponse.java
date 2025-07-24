package iroom.dto;

import java.time.LocalDateTime;

public record DashBoardResponse(
        String metricType,
        Integer metricValue,
        LocalDateTime recordedAt
) {}
