package com.iroom.dashboard.service;

import com.iroom.dashboard.dto.request.BlueprintRequest;
import com.iroom.dashboard.dto.response.BlueprintResponse;
import com.iroom.dashboard.dto.response.PagedResponse;

public interface BlueprintService {

    // 도면 등록
    BlueprintResponse createBlueprint(BlueprintRequest request);

    // 도면 수정
    BlueprintResponse updateBlueprint(Long id, BlueprintRequest request);

    // 도면 삭제
    void deleteBlueprint(Long id);

    // 도면 전체 조회
    PagedResponse<BlueprintResponse> getAllBlueprints(int page, int size);

}
