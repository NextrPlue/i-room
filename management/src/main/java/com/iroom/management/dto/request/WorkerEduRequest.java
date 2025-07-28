package com.iroom.management.dto.request;

import com.iroom.management.entity.WorkerEdu;

import java.time.LocalDate;

public record WorkerEduRequest(
	Long id,
	Long workerId,
	String name,
	String certUrl,
	LocalDate eduDate
) {
	public WorkerEdu toEntity() {
		return WorkerEdu.builder()
			.workerId(workerId)
			.name(name)
			.certUrl(certUrl)
			.eduDate(eduDate)
			.build();
	}
}
