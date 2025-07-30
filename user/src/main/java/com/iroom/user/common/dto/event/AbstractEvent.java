package com.iroom.user.common.dto.event;

import java.time.LocalDateTime;

public record AbstractEvent(
	String eventType,
	LocalDateTime timestamp,
	Object data
) {
}