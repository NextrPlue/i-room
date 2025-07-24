package com.iroom.user.dto.request;

import com.iroom.user.annotation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminUpdatePasswordRequest(
        @NotBlank
        @ValidPassword
        String password,

        @NotBlank
        @ValidPassword
        String newPassword
) {}