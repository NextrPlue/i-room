package com.iroom.management.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iroom.management.entity.WorkerEdu;
import com.iroom.management.repository.WorkerEduRepository;
import com.iroom.management.repository.WorkerReadModelRepository;
import com.iroom.modulecommon.dto.response.PagedResponse;
import com.iroom.modulecommon.exception.CustomException;
import com.iroom.modulecommon.exception.ErrorCode;
import com.iroom.management.dto.request.WorkerEduRequest;
import com.iroom.management.dto.response.WorkerEduResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkerEduService {

	private final WorkerEduRepository workerEduRepository;
	private final WorkerReadModelRepository workerReadModelRepository;

	// 안전교육 내역 기록
	@Transactional
	@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN')")
	public WorkerEduResponse recordEdu(WorkerEduRequest requestDto) {
		// 근로자 존재 여부 확인
		Long workerId = requestDto.workerId();
		if (workerId == null) {
			throw new CustomException(ErrorCode.MANAGEMENT_INVALID_WORKER_ID);
		}

		workerReadModelRepository.findById(workerId)
			.orElseThrow(() -> new CustomException(ErrorCode.MANAGEMENT_WORKER_NOT_FOUND));

		WorkerEdu workerEdu = requestDto.toEntity();
		WorkerEdu saved = workerEduRepository.save(workerEdu);
		return new WorkerEduResponse(saved);
	}

	// 안전교육 내역 조회
	@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN') or (hasAuthority('ROLE_WORKER') and #workerId == authentication.principal)")
	public PagedResponse<WorkerEduResponse> getEduInfo(Long workerId, int page, int size) {
		// 근로자 존재 여부 확인
		if (workerId == null) {
			throw new CustomException(ErrorCode.MANAGEMENT_INVALID_WORKER_ID);
		}

		workerReadModelRepository.findById(workerId)
			.orElseThrow(() -> new CustomException(ErrorCode.MANAGEMENT_WORKER_NOT_FOUND));

		Pageable pageable = PageRequest.of(page, size);
		Page<WorkerEdu> eduPage = workerEduRepository.findAllByWorkerId(workerId, pageable);

		Page<WorkerEduResponse> responsePage = eduPage.map(WorkerEduResponse::new);

		return PagedResponse.of(responsePage);
	}
}
