package com.iroom.modulecommon.dto.event;

import java.time.LocalDateTime;

public record SafetyAlartEvent(
	Long workerId,
	LocalDateTime occuredAt,
	String incidentType,
	Long incidentId,
	String workerLatitude,
	String workerLongitude,
	String incidentDescription
) {
}
