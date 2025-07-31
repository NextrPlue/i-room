package com.iroom.modulecommon.dto.event;

import java.time.LocalDateTime;

import com.iroom.modulecommon.enums.AdminRole;

public record AdminEvent(
	Long id,
	String name,
	String email,
	String phone,
	AdminRole role,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
}