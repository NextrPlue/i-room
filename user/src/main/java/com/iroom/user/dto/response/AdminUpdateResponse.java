package com.iroom.user.dto.response;

public record AdminUpdateResponse(
        Long id,
        String name,
        String email,
        String phone,
        String role
) {}
