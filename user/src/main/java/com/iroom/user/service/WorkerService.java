package com.iroom.user.service;

import com.iroom.user.dto.request.WorkerRegisterRequest;
import com.iroom.user.dto.response.WorkerRegisterResponse;
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
}
