package com.iroom.management.dto.request;

import java.time.LocalDateTime;

public record WorkerManagementRequest(
	Long id,
	Long workerId,
	LocalDateTime enterDate,
	LocalDateTime outDate
) {
}

