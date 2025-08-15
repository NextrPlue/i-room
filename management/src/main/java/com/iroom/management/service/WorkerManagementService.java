package com.iroom.management.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iroom.management.dto.response.WorkerManagementResponse;
import com.iroom.management.dto.response.WorkerStatsResponse;
import com.iroom.management.dto.response.WorkingWorkerResponse;
import com.iroom.management.entity.WorkerManagement;
import com.iroom.management.entity.WorkerReadModel;
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
	// WorkerReadModel에 존재하는 근로자만 입장 가능
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
	// WorkerReadModel에 존재하는 근로자만 퇴장 가능
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

		workerReadModelRepository.findById(workerId)
			.orElseThrow(() -> new CustomException(ErrorCode.MANAGEMENT_WORKER_NOT_FOUND));

		return workerManagementRepository.findTopByWorkerIdOrderByEnterDateDesc(workerId)
			.map(WorkerManagementResponse::new)
			.orElse(new WorkerManagementResponse(null, workerId, null, null));
	}

	// 근로자 출입현황 전체 목록 조회
	@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_READER')")
	public PagedResponse<WorkerManagementResponse> getEntries(String date, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);

		Page<WorkerManagement> entryPage;
		if (date == null || date.trim().isEmpty()) {
			entryPage = workerManagementRepository.findAll(pageable);
		} else {
			try {
				LocalDate searchDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
				LocalDateTime startOfDay = searchDate.atStartOfDay();
				LocalDateTime endOfDay = searchDate.atTime(23, 59, 59);

				entryPage = workerManagementRepository.findByEnterDateBetween(startOfDay, endOfDay, pageable);
			} catch (DateTimeParseException e) {
				entryPage = Page.empty();
			}
		}

		return PagedResponse.of(entryPage.map(WorkerManagementResponse::new));
	}

	// 근로자 출입 통계 조회
	@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_READER')")
	public WorkerStatsResponse getWorkerStatistics() {
		// 전체 근로자 수
		int totalWorkers = (int)workerReadModelRepository.count();

		// 오늘 날짜 범위 설정
		LocalDate today = LocalDate.now();
		LocalDateTime startOfDay = today.atStartOfDay();
		LocalDateTime endOfDay = today.atTime(23, 59, 59);

		// 오늘 출입 기록들 조회
		List<WorkerManagement> todayEntries = workerManagementRepository
			.findByEnterDateBetween(startOfDay, endOfDay, Pageable.unpaged())
			.getContent();

		// 근무중: 오늘 출근했고 아직 퇴근 안함
		int working = (int)todayEntries.stream()
			.filter(entry -> entry.getOutDate() == null)
			.count();

		// 퇴근: 오늘 출근해서 퇴근함
		int offWork = (int)todayEntries.stream()
			.filter(entry -> entry.getOutDate() != null &&
				entry.getOutDate().toLocalDate().equals(today))
			.count();

		// 미출근: 전체 근로자 - 오늘 출근한 사람들
		int todayAttended = todayEntries.size();
		int absent = totalWorkers - todayAttended;

		return new WorkerStatsResponse(totalWorkers, working, offWork, absent);
	}

	// 근로자 본인 출입현황 조회
	@PreAuthorize("hasAuthority('ROLE_WORKER') and #workerId == authentication.principal")
	public WorkerManagementResponse getWorkerEntry(Long workerId) {
		WorkerManagement workerManagement = workerManagementRepository.findById(workerId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_WORKER_NOT_FOUND));

		return new WorkerManagementResponse(workerManagement);
	}

	// 근무중인 근로자 목록 조회
	@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_READER')")
	public List<WorkingWorkerResponse> getWorkingWorkers() {
		// 오늘 날짜 범위 설정
		LocalDate today = LocalDate.now();
		LocalDateTime startOfDay = today.atStartOfDay();
		LocalDateTime endOfDay = today.atTime(23, 59, 59);

		// 오늘 출근했고 아직 퇴근하지 않은 근로자들 직접 조회
		List<WorkerManagement> workingEntries = workerManagementRepository
			.findByEnterDateBetweenAndOutDateIsNull(startOfDay, endOfDay);

		return workingEntries.stream()
			.map(entry -> {
				// WorkerReadModel에서 근로자 상세 정보 조회
				WorkerReadModel workerInfo = workerReadModelRepository.findById(entry.getWorkerId())
					.orElse(null);
				
				String workerName = workerInfo != null ? workerInfo.getName() : "알 수 없음";
				String department = workerInfo != null ? workerInfo.getDepartment() : "미정";
				String occupation = workerInfo != null ? workerInfo.getOccupation() : "미정";
				
				return new WorkingWorkerResponse(entry, workerName, department, occupation);
			})
			.collect(Collectors.toList());
	}
}
