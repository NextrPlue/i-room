package com.iroom.management.service;

import com.iroom.management.dto.request.WorkerEduRequest;
import com.iroom.management.dto.response.PagedResponse;
import com.iroom.management.dto.response.WorkerEduResponse;

import java.util.List;

public interface WorkerEduService {
    // 안전교육 내역 기록
    WorkerEduResponse recordEdu(WorkerEduRequest requestDto);

    // 안전교육 내역 조회
    PagedResponse<WorkerEduResponse> getEduInfo(Long workerId, int page, int size);
}
