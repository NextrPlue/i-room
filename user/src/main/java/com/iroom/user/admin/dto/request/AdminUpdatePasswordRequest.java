package com.iroom.user.admin.dto.request;

import com.iroom.user.common.annotation.ValidPassword;
import jakarta.validation.constraints.NotBlank;

public record AdminUpdatePasswordRequest(
        @NotBlank
        @ValidPassword
        String password,

        @NotBlank
        @ValidPassword
        String newPassword
) {}