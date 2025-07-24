package com.iroom.user.dto.request;

import com.iroom.user.annotation.ValidPassword;
import jakarta.validation.constraints.NotBlank;

public record WorkerUpdatePasswordRequest(
        @NotBlank
        @ValidPassword
        String password,

        @NotBlank
        @ValidPassword
        String newPassword
) {}
