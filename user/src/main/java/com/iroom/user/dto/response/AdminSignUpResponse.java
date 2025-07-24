package com.iroom.user.dto.response;

import com.iroom.user.entity.Admin;
import com.iroom.user.enums.AdminRole;

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
