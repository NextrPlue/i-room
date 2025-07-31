package com.iroom.dashboard.dto.event;

import java.time.LocalDateTime;

public record DangerZoneAlarmEvent(
	Long workerId,
	LocalDateTime occuredAt,
	String incidentType,
	Long incidentId,
	String workerLatitude,
	String workerLongitude,
	String incidentDescription
) {
}
