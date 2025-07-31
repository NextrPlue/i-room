package com.iroom.user.worker.service;

import com.iroom.user.common.dto.response.LoginResponse;
import com.iroom.modulecommon.dto.response.PagedResponse;
import com.iroom.modulecommon.service.KafkaProducerService;
import com.iroom.user.worker.dto.event.WorkerEvent;
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
			throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
		}

		Worker worker = request.toEntity(passwordEncoder);
		workerRepository.save(worker);

		kafkaProducerService.publishMessage("WORKER_CREATED", new WorkerEvent(worker));

		return new WorkerRegisterResponse(worker);
	}

	public LoginResponse login(LoginRequest request) {
		Worker worker = workerRepository.findByEmail(request.email())
			.orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

		if (!passwordEncoder.matches(request.password(), worker.getPassword())) {
			throw new IllegalArgumentException("잘못된 비밀번호입니다.");
		}

		return new LoginResponse(jwtTokenProvider.createWorkerToken(worker));
	}

	@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN')")
	public WorkerUpdateResponse updateWorkerInfo(Long id, WorkerUpdateInfoRequest request) {
		Worker worker = workerRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("해당하는 근로자를 찾을 수 없습니다."));

		if (!worker.getEmail().equals(request.email()) && workerRepository.existsByEmail(request.email())) {
			throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
		}

		worker.updateInfo(request);

		kafkaProducerService.publishMessage("WORKER_UPDATED", new WorkerEvent(worker));

		return new WorkerUpdateResponse(worker);
	}

	@PreAuthorize("hasAnyAuthority('ROLE_WORKER') and #id == authentication.principal")
	public void updateWorkerPassword(Long id, WorkerUpdatePasswordRequest request) {
		Worker worker = workerRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("해당하는 근로자를 찾을 수 없습니다."));

		if (!passwordEncoder.matches(request.password(), worker.getPassword())) {
			throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
		}

		worker.updatePassword(passwordEncoder.encode(request.newPassword()));

		kafkaProducerService.publishMessage("WORKER_UPDATED", new WorkerEvent(worker));
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
			.orElseThrow(() -> new IllegalArgumentException("해당하는 근로자를 찾을 수 없습니다."));

		return new WorkerInfoResponse(worker);
	}

	@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_READER')")
	public WorkerInfoResponse getWorkerById(Long workerId) {
		Worker worker = workerRepository.findById(workerId)
			.orElseThrow(() -> new IllegalArgumentException("해당하는 근로자를 찾을 수 없습니다."));

		return new WorkerInfoResponse(worker);
	}

	@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN')")
	public void deleteWorker(Long workerId) {
		Worker worker = workerRepository.findById(workerId)
			.orElseThrow(() -> new IllegalArgumentException("ID " + workerId + "에 해당하는 근로자를 찾을 수 없습니다."));

		workerRepository.delete(worker);

		kafkaProducerService.publishMessage("WORKER_DELETED", new WorkerEvent(worker));
	}
}
