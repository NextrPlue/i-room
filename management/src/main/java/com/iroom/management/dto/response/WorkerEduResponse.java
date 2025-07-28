package com.iroom.management.dto.response;

import com.iroom.management.entity.WorkerEdu;

import java.time.LocalDate;

public record WorkerEduResponse(
	Long id,
	Long workerId,
	String name,
	String certUrl,
	LocalDate eduDate
) {
	// 엔티티 → DTO로 변환하는 생성자
	public WorkerEduResponse(WorkerEdu entity) {
		this(
			entity.getId(),
			entity.getWorkerId(),
			entity.getName(),
			entity.getCertUrl(),
			entity.getEduDate()
		);
	}
}
