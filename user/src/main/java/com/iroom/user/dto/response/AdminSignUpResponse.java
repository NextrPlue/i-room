package com.iroom.user.dto.response;

public record AdminSignUpResponse(
        String name,
        String email,
        String phone,
        String role
) {}
