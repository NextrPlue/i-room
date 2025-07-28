package com.iroom.management.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.iroom.management.entity.WorkerManagement;

public record WorkerManagementResponse(
	Long id,
	Long workerId,
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	LocalDateTime enterDate,
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	LocalDateTime outDate
) {
	public WorkerManagementResponse(WorkerManagement entity) {
		this(
			entity.getId(),
			entity.getWorkerId(),
			entity.getEnterDate(),
			entity.getOutDate()
		);
	}
}
