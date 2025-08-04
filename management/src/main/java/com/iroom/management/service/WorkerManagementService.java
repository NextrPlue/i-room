package com.iroom.management.service;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iroom.management.dto.response.WorkerManagementResponse;
import com.iroom.management.entity.WorkerManagement;
import com.iroom.management.repository.WorkerManagementRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class WorkerManagementService {

	private final WorkerManagementRepository repository;

	// 근로자 입장
	// 근로자 리드모델 조회로 존재하는 근로자만 동작하도록 수정 필요
	@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_ENTRANCE_SYSTEM')")
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

	// 근로자 퇴장
	// 근로자 리드모델 조회로 존재하는 근로자만 동작하도록 수정 필요
	@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_ENTRANCE_SYSTEM')")
	public WorkerManagementResponse exitWorker(Long workerId) {
		WorkerManagement existing = repository.findByWorkerId(workerId)
			.orElseThrow(() -> new IllegalArgumentException("해당 근로자를 찾을 수 없습니다."));
		existing.markExitedNow();
		WorkerManagement updated = repository.save(existing);
		return new WorkerManagementResponse(updated);
	}

	// 근로자 출입현황 조회
	@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_ENTRANCE_SYSTEM')")
	public WorkerManagementResponse getEntryByWorkerId(Long workerId) {
		if (workerId == null) {
			throw new IllegalArgumentException("workerId는 null일 수 없습니다.");
		}

		return repository.findTopByWorkerIdOrderByEnterDateDesc(workerId)
			.map(WorkerManagementResponse::new)
			.orElse(new WorkerManagementResponse(null, workerId, null, null));
	}
}
