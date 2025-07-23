package com.iroom.user.dto.request;

import com.iroom.user.enums.AdminRole;
import jakarta.validation.constraints.NotNull;

public record AdminUpdateRoleRequest(
        @NotNull
        AdminRole role
) {}