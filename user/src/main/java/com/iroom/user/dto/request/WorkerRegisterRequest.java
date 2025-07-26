package com.iroom.user.dto.request;

import com.iroom.user.annotation.ValidPassword;
import com.iroom.user.annotation.ValidPhone;
import com.iroom.user.entity.Worker;
import com.iroom.user.enums.WorkerRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.crypto.password.PasswordEncoder;

public record WorkerRegisterRequest(
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
        String phone,

        String bloodType,
        String gender,
        Integer age,
        Float weight,
        Float height,
        String jobTitle,
        String occupation,
        String department,
        String faceImageUrl
) {
    public Worker toEntity(PasswordEncoder encoder) {
        return Worker.builder()
                .name(this.name)
                .email(this.email)
                .password(encoder.encode(this.password))
                .phone(this.phone)
                .role(WorkerRole.WORKER)
                .bloodType(this.bloodType)
                .gender(this.gender)
                .age(this.age)
                .weight(this.weight)
                .height(this.height)
                .jobTitle(this.jobTitle)
                .occupation(this.occupation)
                .department(this.department)
                .faceImageUrl(this.faceImageUrl)
                .build();
    }
}
