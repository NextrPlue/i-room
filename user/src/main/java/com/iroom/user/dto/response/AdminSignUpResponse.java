package com.iroom.user.dto.response;

import com.iroom.user.enums.AdminRole;

public record AdminSignUpResponse(
        String name,
        String email,
        String phone,
        AdminRole role
) {}
