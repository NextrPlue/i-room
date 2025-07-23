package com.iroom.user.dto.response;

import com.iroom.user.enums.AdminRole;

public record AdminUpdateResponse(
        Long id,
        String name,
        String email,
        String phone,
        AdminRole role
) {}
