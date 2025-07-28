package com.iroom.management.service;

import org.springframework.stereotype.Service;

import com.iroom.management.dto.response.WorkerManagementResponse;
import com.iroom.management.entity.WorkerManagement;
import com.iroom.management.repository.WorkerManagementRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkerManagementServiceImpl implements WorkerManagementService {
	private final WorkerManagementRepository repository;

	@Override
	public WorkerManagementResponse enterWorker(Long workerId) {
		if (workerId == null) {
			throw new IllegalArgumentException("workerId는 null일 수 없습니다.");
		}
		WorkerManagement workerManagement = WorkerManagement.builder()
			.workerId(workerId)
			.build();
		WorkerManagement saved = repository.save(workerManagement);
		return new WorkerManagementResponse(saved);
	}

	@Override
	public WorkerManagementResponse exitWorker(Long workerId) {
		WorkerManagement existing = repository.findByWorkerId(workerId)
			.orElseThrow(() -> new IllegalArgumentException("해당 근로자를 찾을 수 없습니다."));
		existing.markExitedNow();
		WorkerManagement updated = repository.save(existing);
		return new WorkerManagementResponse(updated);
	}
}
