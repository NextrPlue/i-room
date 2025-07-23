package com.iroom.user.dto.request;

import com.iroom.user.enums.AdminRole;

public record AdminSignUpRequest(
        String name,
        String email,
        String password,
        String phone,
        AdminRole role
) {}
