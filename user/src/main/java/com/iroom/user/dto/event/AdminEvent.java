package com.iroom.user.dto.event;

import java.time.LocalDateTime;

import com.iroom.user.entity.Admin;
import com.iroom.user.enums.AdminRole;

public record AdminEvent(
	Long id,
	String name,
	String email,
	String phone,
	AdminRole role,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
	public AdminEvent(Admin admin) {
		this(
			admin.getId(),
			admin.getName(),
			admin.getEmail(),
			admin.getPhone(),
			admin.getRole(),
			admin.getCreatedAt(),
			admin.getUpdatedAt());
	}
}