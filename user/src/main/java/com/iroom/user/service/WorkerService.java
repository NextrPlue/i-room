package com.iroom.user.service;

import com.iroom.user.dto.request.LoginRequest;
import com.iroom.user.dto.request.WorkerRegisterRequest;
import com.iroom.user.dto.request.WorkerUpdateInfoRequest;
import com.iroom.user.dto.request.WorkerUpdatePasswordRequest;
import com.iroom.user.dto.response.LoginResponse;
import com.iroom.user.dto.response.WorkerRegisterResponse;
import com.iroom.user.dto.response.WorkerUpdateResponse;
import com.iroom.user.entity.Worker;
import com.iroom.user.jwt.JwtTokenProvider;
import com.iroom.user.repository.WorkerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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

    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN')")
    public WorkerRegisterResponse registerWorker(WorkerRegisterRequest request) {
        if (workerRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        Worker worker = request.toEntity(passwordEncoder);
        workerRepository.save(worker);

        return new WorkerRegisterResponse(worker);
    }

    public LoginResponse login(LoginRequest request) {
        Worker worker = workerRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("해당하는 근로자를 찾을 수 없습니다."));

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
    }
}
