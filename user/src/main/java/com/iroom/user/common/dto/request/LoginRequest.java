package com.iroom.user.common.dto.request;

import com.iroom.user.common.annotation.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank
        @Email
        String email,

        @NotBlank
        @ValidPassword
        String password
) {}
