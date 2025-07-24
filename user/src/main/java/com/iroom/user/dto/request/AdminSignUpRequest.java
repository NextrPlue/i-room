package com.iroom.user.dto.request;

import com.iroom.user.annotation.ValidPhone;
import com.iroom.user.entity.Admin;
import com.iroom.user.enums.AdminRole;
import com.iroom.user.annotation.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.crypto.password.PasswordEncoder;

public record AdminSignUpRequest(
        @NotBlank
        @Size(min = 2, max = 20)
        String name,

        @NotBlank
        @Email
        String email,

        @NotBlank
        @ValidPassword
        String password,

        @NotBlank
        @ValidPhone
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
