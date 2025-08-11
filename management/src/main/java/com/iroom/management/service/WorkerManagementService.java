package com.iroom.management.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iroom.management.dto.response.WorkerEduResponse;
import com.iroom.management.dto.response.WorkerManagementResponse;
import com.iroom.management.entity.WorkerEdu;
import com.iroom.management.entity.WorkerManagement;
import com.iroom.management.repository.WorkerManagementRepository;
import com.iroom.management.repository.WorkerReadModelRepository;
import com.iroom.modulecommon.dto.response.PagedResponse;
import com.iroom.modulecommon.exception.CustomException;
import com.iroom.modulecommon.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class WorkerManagementService {

	private final WorkerManagementRepository workerManagementRepository;
	private final WorkerReadModelRepository workerReadModelRepository;

	// 근로자 입장
	// 근로자 리드모델 조회로 존재하는 근로자만 동작하도록 수정 필요
	@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_ENTRANCE_SYSTEM')")
	public WorkerManagementResponse enterWorker(Long workerId) {
		if (workerId == null) {
			throw new CustomException(ErrorCode.MANAGEMENT_INVALID_WORKER_ID);
		}

		workerReadModelRepository.findById(workerId)
			.orElseThrow(() -> new CustomException(ErrorCode.MANAGEMENT_WORKER_NOT_FOUND));

		Optional<WorkerManagement> activeEntry = workerManagementRepository
			.findByWorkerIdAndOutDateIsNull(workerId);
		if (activeEntry.isPresent()) {
			throw new CustomException(ErrorCode.MANAGEMENT_WORKER_ALREADY_ENTERED);
		}

		WorkerManagement workerManagement = WorkerManagement.builder()
			.workerId(workerId)
			.build();
		WorkerManagement saved = workerManagementRepository.save(workerManagement);
		return new WorkerManagementResponse(saved);
	}

	// 근로자 퇴장
	// 근로자 리드모델 조회로 존재하는 근로자만 동작하도록 수정 필요
	@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_ENTRANCE_SYSTEM')")
	public WorkerManagementResponse exitWorker(Long workerId) {
		if (workerId == null) {
			throw new CustomException(ErrorCode.MANAGEMENT_INVALID_WORKER_ID);
		}

		workerReadModelRepository.findById(workerId)
			.orElseThrow(() -> new CustomException(ErrorCode.MANAGEMENT_WORKER_NOT_FOUND));

		WorkerManagement activeEntry = workerManagementRepository
			.findByWorkerIdAndOutDateIsNull(workerId)
			.orElseThrow(() -> new CustomException(ErrorCode.MANAGEMENT_WORKER_NOT_ENTERED));

		activeEntry.markExitedNow();
		WorkerManagement updated = workerManagementRepository.save(activeEntry);
		return new WorkerManagementResponse(updated);
	}

	// 근로자 출입현황 조회
	@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_READER')")
	public WorkerManagementResponse getEntryByWorkerId(Long workerId) {
		if (workerId == null) {
			throw new CustomException(ErrorCode.MANAGEMENT_INVALID_WORKER_ID);
		}

		return workerManagementRepository.findTopByWorkerIdOrderByEnterDateDesc(workerId)
			.map(WorkerManagementResponse::new)
			.orElse(new WorkerManagementResponse(null, workerId, null, null));
	}

	// 근로자 본인 출입현황 조회
	@PreAuthorize("hasAuthority('ROLE_WORKER') and #id == authentication.principal")
	public WorkerManagementResponse getWorkerEntry(Long workerId) {
		WorkerManagement workerManagement = workerManagementRepository.findById(workerId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_WORKER_NOT_FOUND));

		return new WorkerManagementResponse(workerManagement);
	}
}
