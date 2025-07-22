package com.iroom.management.service;

import com.iroom.management.dto.request.WorkerEduRequest;
import com.iroom.management.dto.response.WorkerEduResponse;

public interface WorkerEduService {
    // 안전교육 내역 기록
    WorkerEduResponse recordEdu(WorkerEduRequest requestDto);

    // 안전교육 내역 조회
    WorkerEduResponse getEduInfo(Long workerId);
}
