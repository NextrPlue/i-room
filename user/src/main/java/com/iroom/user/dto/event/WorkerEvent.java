package com.iroom.user.dto.event;

import com.iroom.user.entity.Worker;
import com.iroom.user.enums.BloodType;
import com.iroom.user.enums.Gender;
import com.iroom.user.enums.WorkerRole;

public record WorkerEvent(
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
	String faceImageUrl
) {
	public WorkerEvent(Worker worker) {
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
			worker.getFaceImageUrl()
		);
	}
}
