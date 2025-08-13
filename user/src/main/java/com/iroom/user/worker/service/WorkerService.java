package com.iroom.user.worker.service;

import com.iroom.modulecommon.dto.response.SimpleResponse;
import com.iroom.modulecommon.exception.CustomException;
import com.iroom.modulecommon.exception.ErrorCode;
import com.iroom.user.common.dto.response.LoginResponse;
import com.iroom.modulecommon.dto.response.PagedResponse;
import com.iroom.modulecommon.service.KafkaProducerService;
import com.iroom.modulecommon.dto.event.WorkerEvent;
import com.iroom.user.common.dto.request.LoginRequest;
import com.iroom.user.worker.dto.request.WorkerRegisterRequest;
import com.iroom.user.worker.dto.request.WorkerUpdateInfoRequest;
import com.iroom.user.worker.dto.request.WorkerUpdatePasswordRequest;
import com.iroom.user.worker.dto.response.WorkerInfoResponse;
import com.iroom.user.worker.dto.response.WorkerRegisterResponse;
import com.iroom.user.worker.dto.response.WorkerUpdateResponse;
import com.iroom.user.worker.entity.Worker;
import com.iroom.user.common.jwt.JwtTokenProvider;
import com.iroom.user.worker.repository.WorkerRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class WorkerService {

	private final WorkerRepository workerRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final KafkaProducerService kafkaProducerService;

	@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN')")
	public WorkerRegisterResponse registerWorker(WorkerRegisterRequest request) {
		if (workerRepository.existsByEmail(request.email())) {
			throw new CustomException(ErrorCode.USER_EMAIL_ALREADY_EXISTS);
		}

		Worker worker = request.toEntity(passwordEncoder);
		workerRepository.save(worker);

		WorkerEvent workerEvent = new WorkerEvent(worker.getId(), worker.getName(), worker.getEmail(),
			worker.getPhone(),
			worker.getRole(), worker.getBloodType(), worker.getGender(), worker.getAge(), worker.getWeight(),
			worker.getHeight(), worker.getJobTitle(), worker.getOccupation(), worker.getDepartment(),
			worker.getFaceImageUrl(), worker.getCreatedAt(), worker.getUpdatedAt());

		kafkaProducerService.publishMessage("WORKER_CREATED", workerEvent);

		return new WorkerRegisterResponse(worker);
	}

	public LoginResponse login(LoginRequest request) {
		Worker worker = workerRepository.findByEmail(request.email())
			.orElseThrow(() -> new CustomException(ErrorCode.USER_UNREGISTERED_EMAIL));

		if (!passwordEncoder.matches(request.password(), worker.getPassword())) {
			throw new CustomException(ErrorCode.USER_INVALID_PASSWORD);
		}

		return new LoginResponse(jwtTokenProvider.createWorkerToken(worker));
	}

	@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN')")
	public WorkerUpdateResponse updateWorkerInfo(Long id, WorkerUpdateInfoRequest request) {
		Worker worker = workerRepository.findById(id)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_WORKER_NOT_FOUND));

		if (!worker.getEmail().equals(request.email()) && workerRepository.existsByEmail(request.email())) {
			throw new CustomException(ErrorCode.USER_EMAIL_ALREADY_EXISTS);
		}

		worker.updateInfo(request);

		WorkerEvent workerEvent = new WorkerEvent(worker.getId(), worker.getName(), worker.getEmail(),
			worker.getPhone(),
			worker.getRole(), worker.getBloodType(), worker.getGender(), worker.getAge(), worker.getWeight(),
			worker.getHeight(), worker.getJobTitle(), worker.getOccupation(), worker.getDepartment(),
			worker.getFaceImageUrl(), worker.getCreatedAt(), worker.getUpdatedAt());

		kafkaProducerService.publishMessage("WORKER_UPDATED", workerEvent);

		return new WorkerUpdateResponse(worker);
	}

	@PreAuthorize("hasAnyAuthority('ROLE_WORKER') and #id == authentication.principal")
	public SimpleResponse updateWorkerPassword(Long id, WorkerUpdatePasswordRequest request) {
		Worker worker = workerRepository.findById(id)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_WORKER_NOT_FOUND));

		if (!passwordEncoder.matches(request.password(), worker.getPassword())) {
			throw new CustomException(ErrorCode.USER_CURRENT_PASSWORD_MISMATCH);
		}

		worker.updatePassword(passwordEncoder.encode(request.newPassword()));

		WorkerEvent workerEvent = new WorkerEvent(worker.getId(), worker.getName(), worker.getEmail(),
			worker.getPhone(),
			worker.getRole(), worker.getBloodType(), worker.getGender(), worker.getAge(), worker.getWeight(),
			worker.getHeight(), worker.getJobTitle(), worker.getOccupation(), worker.getDepartment(),
			worker.getFaceImageUrl(), worker.getCreatedAt(), worker.getUpdatedAt());

		kafkaProducerService.publishMessage("WORKER_UPDATED", workerEvent);

		return new SimpleResponse("비밀번호가 성공적으로 변경되었습니다.");
	}

	@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_READER')")
	public PagedResponse<WorkerInfoResponse> getWorkers(String target, String keyword, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);

		Page<Worker> workerPage;
		if (target == null || keyword == null || keyword.trim().isEmpty()) {
			workerPage = workerRepository.findAll(pageable);
		} else if ("name".equals(target)) {
			workerPage = workerRepository.findByNameContaining(keyword, pageable);
		} else if ("email".equals(target)) {
			workerPage = workerRepository.findByEmailContaining(keyword, pageable);
		} else {
			workerPage = workerRepository.findAll(pageable);
		}

		Page<WorkerInfoResponse> responsePage = workerPage.map(WorkerInfoResponse::new);

		return PagedResponse.of(responsePage);
	}

	@PreAuthorize("hasAuthority('ROLE_WORKER') and #id == authentication.principal")
	public WorkerInfoResponse getWorkerInfo(Long id) {
		Worker worker = workerRepository.findById(id)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_WORKER_NOT_FOUND));

		return WorkerInfoResponse.maskedFrom(worker);
	}

	@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_READER')")
	public WorkerInfoResponse getWorkerById(Long workerId) {
		Worker worker = workerRepository.findById(workerId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_WORKER_NOT_FOUND));

		return new WorkerInfoResponse(worker);
	}

	@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN')")
	public SimpleResponse deleteWorker(Long workerId) {
		Worker worker = workerRepository.findById(workerId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_WORKER_NOT_FOUND));

		workerRepository.delete(worker);

		WorkerEvent workerEvent = new WorkerEvent(worker.getId(), worker.getName(), worker.getEmail(),
			worker.getPhone(),
			worker.getRole(), worker.getBloodType(), worker.getGender(), worker.getAge(), worker.getWeight(),
			worker.getHeight(), worker.getJobTitle(), worker.getOccupation(), worker.getDepartment(),
			worker.getFaceImageUrl(), worker.getCreatedAt(), worker.getUpdatedAt());

		kafkaProducerService.publishMessage("WORKER_DELETED", workerEvent);

		return new SimpleResponse("근로자가 성공적으로 삭제되었습니다.");
	}
}
