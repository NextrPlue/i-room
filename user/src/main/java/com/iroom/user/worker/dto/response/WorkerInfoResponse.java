package com.iroom.user.worker.dto.response;

import com.iroom.user.worker.entity.Worker;
import com.iroom.user.worker.util.MaskingUtil;
import com.iroom.modulecommon.enums.BloodType;
import com.iroom.modulecommon.enums.Gender;
import com.iroom.modulecommon.enums.WorkerRole;

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

	public static WorkerInfoResponse maskedFrom(Worker w) {
		return new WorkerInfoResponse(
			w.getId(),
			MaskingUtil.maskNameMiddleOne(w.getName()),
			MaskingUtil.maskEmailFirst3(w.getEmail()),
			MaskingUtil.maskPhoneMiddle4(w.getPhone()),
			w.getRole(),
			w.getBloodType(),
			w.getGender(),
			w.getAge(),
			w.getWeight(),
			w.getHeight(),
			w.getJobTitle(),
			w.getOccupation(),
			w.getDepartment(),
			w.getFaceImageUrl(),
			w.getCreatedAt(),
			w.getUpdatedAt()
		);
	}
}
