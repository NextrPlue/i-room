package com.iroom.admin.dto.request;

public record SignUpRequest(
        String name,
        String email,
        String password,
        String phone,
        String role
) {}
