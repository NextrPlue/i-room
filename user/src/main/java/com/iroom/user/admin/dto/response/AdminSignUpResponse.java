package com.iroom.user.admin.dto.response;

import com.iroom.user.admin.entity.Admin;
import com.iroom.user.admin.enums.AdminRole;

public record AdminSignUpResponse(
        String name,
        String email,
        String phone,
        AdminRole role
) {
    public AdminSignUpResponse(Admin admin) {
        this(admin.getName(), admin.getEmail(), admin.getPhone(), admin.getRole());
    }
}
