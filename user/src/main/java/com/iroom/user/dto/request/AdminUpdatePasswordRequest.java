package com.iroom.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminUpdatePasswordRequest(
        @NotBlank
        @Size(min = 6)
        String password,

        @NotBlank
        @Size(min = 6)
        String newPassword
) {}