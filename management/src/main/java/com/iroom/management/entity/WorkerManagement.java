package com.iroom.management.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "WorkerManagement")
public class WorkerManagement {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long workerId;

	private LocalDateTime enterDate;

	private LocalDateTime outDate;

	@PrePersist
	protected void onCreate() {
		this.enterDate = LocalDateTime.now();
	}

	public void markExitedNow() {
		this.outDate = LocalDateTime.now();
	}

	@Builder
	public WorkerManagement(Long workerId) {
		this.workerId = workerId;
	}
}
