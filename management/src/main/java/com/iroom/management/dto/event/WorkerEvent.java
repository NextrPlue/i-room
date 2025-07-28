package com.iroom.management.dto.event;

import com.iroom.management.enums.BloodType;
import com.iroom.management.enums.Gender;
import com.iroom.management.enums.WorkerRole;

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
) {}
