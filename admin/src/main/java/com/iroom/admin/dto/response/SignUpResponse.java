package com.iroom.admin.dto.response;

public record SignUpResponse(
        String name,
        String email,
        String phone,
        String role
) {}
