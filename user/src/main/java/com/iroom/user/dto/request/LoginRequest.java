package com.iroom.user.dto.request;

import com.iroom.user.annotation.ValidPassword;
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
