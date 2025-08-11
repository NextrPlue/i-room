package com.iroom.dashboard.dashboard.entity;

import com.iroom.modulecommon.dto.event.WorkerEvent;
import com.iroom.modulecommon.dto.event.WorkerSensorEvent;
import com.iroom.modulecommon.enums.BloodType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "WorkerReadModel")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class WorkerInfoReadModel {
	@Id
	private Long id;

	private Double latitude;
	private Double longitude;
	private Double heartRate;

	public void updateFromEvent(WorkerSensorEvent event) {
		this.id = event.workerId();
		this.latitude = event.latitude();
		this.longitude = event.longitude();
		this.heartRate = event.heartRate();
	}
}
