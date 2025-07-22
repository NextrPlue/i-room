package com.iroom.management.service;

import com.iroom.management.dto.request.WorkerEduRequest;
import com.iroom.management.dto.response.WorkerEduResponse;
import com.iroom.management.entity.WorkerEdu;
import com.iroom.management.repository.WorkerEduRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkerEduServiceImpl implements WorkerEduService {

    private final WorkerEduRepository workerEduRepository;

    @Transactional
    @Override
    public WorkerEduResponse recordEdu(WorkerEduRequest requestDto) {
        WorkerEdu workerEdu = requestDto.toEntity();
        WorkerEdu saved = workerEduRepository.save(workerEdu);
        return new WorkerEduResponse(saved);
    }

    @Override
    public WorkerEduResponse getEduInfo(Long workerId) {
        WorkerEdu edu = workerEduRepository.findById(workerId)
                .orElseThrow(() -> new IllegalArgumentException("해당 근로자의 교육 이력이 없습니다."));
        return new WorkerEduResponse(edu);
    }
}
