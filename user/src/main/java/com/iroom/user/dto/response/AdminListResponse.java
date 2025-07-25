package com.iroom.user.dto.response;

import com.iroom.user.entity.Admin;
import com.iroom.user.enums.AdminRole;

import java.time.LocalDateTime;
import java.util.List;

public record AdminListResponse(
        Long id,
        String name,
        String email,
        String phone,
        AdminRole role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public AdminListResponse(Admin admin) {
        this(admin.getId(), admin.getName(), admin.getEmail(), admin.getPhone(), admin.getRole(), admin.getCreatedAt(), admin.getUpdatedAt());
    }

    public static List<AdminListResponse> fromList(List<Admin> admins) {
        return admins.stream()
                .map(AdminListResponse::new)
                .toList();
    }
}
