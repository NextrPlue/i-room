package com.iroom.user.admin.dto.response;

import com.iroom.user.admin.entity.Admin;
import com.iroom.modulecommon.enums.AdminRole;

import java.time.LocalDateTime;
import java.util.List;

public record AdminInfoResponse(
        Long id,
        String name,
        String email,
        String phone,
        AdminRole role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public AdminInfoResponse(Admin admin) {
        this(
                admin.getId(),
                admin.getName(),
                admin.getEmail(),
                admin.getPhone(),
                admin.getRole(),
                admin.getCreatedAt(),
                admin.getUpdatedAt());
    }

    public static List<AdminInfoResponse> fromList(List<Admin> admins) {
        return admins.stream()
                .map(AdminInfoResponse::new)
                .toList();
    }
}
