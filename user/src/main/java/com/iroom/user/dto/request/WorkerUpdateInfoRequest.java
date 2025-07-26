package com.iroom.user.dto.request;

import com.iroom.user.annotation.ValidPhone;
import com.iroom.user.enums.BloodType;
import com.iroom.user.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WorkerUpdateInfoRequest(
        @NotBlank
        @Size(min = 2, max = 20)
        String name,

        @NotBlank
        @Email
        String email,

        @NotBlank
        @ValidPhone
        String phone,

        BloodType bloodType,
        Gender gender,
        Integer age,
        Float weight,
        Float height,
        String jobTitle,
        String occupation,
        String department,
        String faceImageUrl
) {
}
