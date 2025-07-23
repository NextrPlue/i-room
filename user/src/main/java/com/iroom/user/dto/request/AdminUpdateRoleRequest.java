package com.iroom.user.dto.request;

import jakarta.validation.constraints.NotNull;

public record AdminUpdateRoleRequest(
        @NotNull
        String role
) {}