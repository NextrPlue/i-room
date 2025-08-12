package com.iroom.management.dto.response;

public record WorkerStatsResponse(
	int total,
	int working,
	int offWork,
	int absent
) {
}