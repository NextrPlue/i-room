package com.iroom.dashboard.dashboard.dto.response;

import java.time.LocalDate;

public record MetricResponse(
	LocalDate getWeekStart,
	String getMetricType,
	Integer getTotalValue
){
}
