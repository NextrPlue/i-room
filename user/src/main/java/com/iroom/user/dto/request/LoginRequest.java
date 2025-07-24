package com.iroom.user.dto.request;

import com.iroom.user.annotation.ValidPassword;

public record LoginRequest(
        String email,

        @ValidPassword
        String password
) {}
