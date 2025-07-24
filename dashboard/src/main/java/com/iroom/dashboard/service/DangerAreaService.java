package com.iroom.dashboard.service;

import com.iroom.dashboard.dto.request.DangerAreaRequest;
import com.iroom.dashboard.dto.response.DangerAreaResponse;

import java.util.List;

public interface DangerAreaService {

    // 위험구역 등록
    DangerAreaResponse create(DangerAreaRequest request);

    // 위험구역 수정
    DangerAreaResponse update(Long id, DangerAreaRequest request);

    // 위험구역 삭제
    void delete(Long id);

    // 위험구역 조회
    List<DangerAreaResponse> getAll();
}
