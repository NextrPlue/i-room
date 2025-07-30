package com.iroom.user.system.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SystemAuthRequest(
        @NotBlank
        String apiKey
) {}
