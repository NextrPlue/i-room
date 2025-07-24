package com.iroom.user.dto.request;

import com.iroom.user.annotation.ValidPhone;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminUpdateInfoRequest(
        @NotBlank
        @Size(min = 2, max = 20)
        String name,

        @NotBlank
        @Email
        String email,

        @NotBlank
        @ValidPhone
        String phone
) {}