package com.iroom.modulecommon.dto.event;

import java.time.LocalDateTime;

public record AlarmEvent(
	Long workerId,
	LocalDateTime occurredAt,
	String incidentType,
	Long incidentId,
	Double workerLatitude,
	Double workerLongitude,
	String incidentDescription,
	String workerImageUrl
) {
}
