package com.iroom.user.admin.dto.request;

import com.iroom.user.admin.enums.AdminRole;
import jakarta.validation.constraints.NotNull;

public record AdminUpdateRoleRequest(
        @NotNull
        AdminRole role
) {}