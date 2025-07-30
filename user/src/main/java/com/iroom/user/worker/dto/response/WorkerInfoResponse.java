package com.iroom.user.worker.dto.response;

import com.iroom.user.worker.entity.Worker;
import com.iroom.user.worker.enums.BloodType;
import com.iroom.user.worker.enums.Gender;
import com.iroom.user.worker.enums.WorkerRole;

import java.time.LocalDateTime;
import java.util.List;

public record WorkerInfoResponse(
        Long id,
        String name,
        String email,
        String phone,
        WorkerRole role,
        BloodType bloodType,
        Gender gender,
        Integer age,
        Float weight,
        Float height,
        String jobTitle,
        String occupation,
        String department,
        String faceImageUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public WorkerInfoResponse(Worker worker) {
        this(
                worker.getId(),
                worker.getName(),
                worker.getEmail(),
                worker.getPhone(),
                worker.getRole(),
                worker.getBloodType(),
                worker.getGender(),
                worker.getAge(),
                worker.getWeight(),
                worker.getHeight(),
                worker.getJobTitle(),
                worker.getOccupation(),
                worker.getDepartment(),
                worker.getFaceImageUrl(),
                worker.getCreatedAt(),
                worker.getUpdatedAt());
    }

    public static List<WorkerInfoResponse> fromList(List<Worker> workers) {
        return workers.stream()
                .map(WorkerInfoResponse::new)
                .toList();
    }
}
