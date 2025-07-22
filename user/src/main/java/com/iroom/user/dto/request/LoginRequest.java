package com.iroom.user.dto.request;

public record LoginRequest(
        String email,
        String password
) {}
