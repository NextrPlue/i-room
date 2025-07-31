package com.iroom.user.admin.dto.request;

import com.iroom.modulecommon.enums.AdminRole;
import jakarta.validation.constraints.NotNull;

public record AdminUpdateRoleRequest(
        @NotNull
        AdminRole role
) {}