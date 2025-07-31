package com.iroom.sensor.dto.event;

import java.time.LocalDateTime;

public record AbstractEvent(
	String eventType,
	LocalDateTime timestamp,
	Object data
) {
}