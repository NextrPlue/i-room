package com.iroom.user.dto.request;

public record AdminSignUpRequest(
        String name,
        String email,
        String password,
        String phone,
        String role
) {}
