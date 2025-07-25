package com.iroom.management.service;

import com.iroom.management.dto.request.WorkerEduRequest;
import com.iroom.management.dto.response.PagedResponse;
import com.iroom.management.dto.response.WorkerEduResponse;
import com.iroom.management.entity.WorkerEdu;
import com.iroom.management.repository.WorkerEduRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public PagedResponse<WorkerEduResponse> getEduInfo(Long workerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<WorkerEdu> eduPage = workerEduRepository.findAllByWorkerId(workerId, pageable);

        Page<WorkerEduResponse> responsePage = eduPage.map(WorkerEduResponse::new);

        return PagedResponse.of(responsePage);
    }
}
