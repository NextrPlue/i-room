package com.iroom.management.service;

import com.iroom.management.entity.WorkerManagement;
import com.iroom.management.dto.request.WorkerManagementRequestDto;
import com.iroom.management.dto.response.WorkerManagementResponseDto;
import com.iroom.management.repository.WorkerManagementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class WorkerManagementServiceImpl implements WorkerManagementService {
    private final WorkerManagementRepository repository;

    @Override
    public WorkerManagementResponseDto enterWorker(WorkerManagementRequestDto requestDto) {
        WorkerManagement workerManagement = requestDto.toEntity();
        workerManagement.setEnterDate(new Date()); // 입장 시간 기록
        WorkerManagement saved = repository.save(workerManagement);
        return new WorkerManagementResponseDto(saved);
    }

    @Override
    public WorkerManagementResponseDto exitWorker(Long workerId) {
        WorkerManagement existing = repository.findByWorkerId(workerId)
                .orElseThrow(()-> new IllegalArgumentException("해당 근로자를 찾을 수 없습니다."));
        existing.setOutDate(new Date());
        WorkerManagement updated = repository.save(existing);
        return new WorkerManagementResponseDto(updated);
    }
}
