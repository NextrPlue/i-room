package com.iroom.user.dto.request;

import com.iroom.user.entity.Admin;
import com.iroom.user.enums.AdminRole;
import org.springframework.security.crypto.password.PasswordEncoder;

public record AdminSignUpRequest(
        String name,
        String email,
        String password,
        String phone
) {
    public Admin toEntity(PasswordEncoder encoder) {
        return Admin.builder()
                .name(this.name)
                .email(this.email)
                .password(encoder.encode(this.password))
                .phone(this.phone)
                .role(AdminRole.READER)
                .build();
    }
}
