package com.iroom.management.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iroom.management.entity.WorkerEdu;
import com.iroom.management.repository.WorkerEduRepository;
import com.iroom.modulecommon.dto.response.PagedResponse;
import com.iroom.management.dto.request.WorkerEduRequest;
import com.iroom.management.dto.response.WorkerEduResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkerEduService {

	private final WorkerEduRepository workerEduRepository;

	// 안전교육 내역 기록
	// 근로자 리드모델 조회로 존재하는 근로자만 동작하도록 수정 필요
	@Transactional
	@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN')")
	public WorkerEduResponse recordEdu(WorkerEduRequest requestDto) {
		WorkerEdu workerEdu = requestDto.toEntity();
		WorkerEdu saved = workerEduRepository.save(workerEdu);
		return new WorkerEduResponse(saved);
	}

	// 안전교육 내역 조회
	// 근로자 리드모델 조회로 존재하는 근로자만 동작하도록 수정 필요
	@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN') or hasAuthority('ROLE_WORKER') and #workerId == authentication.principal")
	public PagedResponse<WorkerEduResponse> getEduInfo(Long workerId, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<WorkerEdu> eduPage = workerEduRepository.findAllByWorkerId(workerId, pageable);

		Page<WorkerEduResponse> responsePage = eduPage.map(WorkerEduResponse::new);

		return PagedResponse.of(responsePage);
	}
}
