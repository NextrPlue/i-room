package com.iroom.dashboard.service;

import com.iroom.dashboard.dto.request.DangerAreaRequest;
import com.iroom.dashboard.dto.response.DangerAreaResponse;
import com.iroom.dashboard.dto.response.PagedResponse;
import com.iroom.dashboard.entity.DangerArea;
import com.iroom.dashboard.repository.DangerAreaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class DangerAreaService {

    private final DangerAreaRepository dangerAreaRepository;

    public DangerAreaResponse createDangerArea(DangerAreaRequest request) {
        DangerArea dangerArea = DangerArea.builder()
                .blueprintId(request.blueprintId())
                .location(request.location())
                .width(request.width())
                .height(request.height())
                .build();
        return new DangerAreaResponse(dangerAreaRepository.save(dangerArea));
    }

    public DangerAreaResponse updateDangerArea(Long id, DangerAreaRequest request) {
        DangerArea dangerArea = dangerAreaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 위험구역이 존재하지 않습니다."));
        dangerArea.update(request.location(), request.width(), request.height());
        return new DangerAreaResponse(dangerArea);
    }

    public void deleteDangerArea(Long id) {
        if (!dangerAreaRepository.existsById(id)) {
            throw new IllegalArgumentException("해당 위험구역이 존재하지 않습니다.");
        }
        dangerAreaRepository.deleteById(id);
    }


    public PagedResponse<DangerAreaResponse> getAllDangerAreas(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<DangerArea> dangerAreas = dangerAreaRepository.findAll(pageable);
        Page<DangerAreaResponse> responsePage = dangerAreas.map(DangerAreaResponse::new);
        return PagedResponse.of(responsePage);
    }
}
