package com.iroom.user.worker.dto.request;

import com.iroom.user.common.annotation.ValidPassword;
import jakarta.validation.constraints.NotBlank;

public record WorkerUpdatePasswordRequest(
        @NotBlank
        @ValidPassword
        String password,

        @NotBlank
        @ValidPassword
        String newPassword
) {}
