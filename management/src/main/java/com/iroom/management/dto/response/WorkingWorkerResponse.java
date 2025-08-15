package com.iroom.management.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.iroom.management.entity.WorkerManagement;

public record WorkingWorkerResponse(
	Long workerId,
	String workerName,
	String department,
	String occupation,
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	LocalDateTime enterDate
) {
	public WorkingWorkerResponse(WorkerManagement entity, String workerName, String department, String occupation) {
		this(
			entity.getWorkerId(),
			workerName,
			department,
			occupation,
			entity.getEnterDate()
		);
	}
}