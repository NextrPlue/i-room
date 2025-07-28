package com.iroom.user.dto.event;

import java.time.LocalDateTime;

public record AbstractEvent(
	String eventType,
	LocalDateTime timestamp,
	Object data
) {
}