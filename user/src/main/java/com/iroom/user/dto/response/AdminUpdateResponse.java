package com.iroom.user.dto.response;

import com.iroom.user.entity.Admin;
import com.iroom.user.enums.AdminRole;

public record AdminUpdateResponse(
        Long id,
        String name,
        String email,
        String phone,
        AdminRole role
) {
    public AdminUpdateResponse(Admin admin) {
        this(admin.getId(), admin.getName(), admin.getEmail(), admin.getPhone(), admin.getRole());
    }
}
