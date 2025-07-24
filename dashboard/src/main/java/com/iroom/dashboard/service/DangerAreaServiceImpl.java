package com.iroom.dashboard.service;

import com.iroom.dashboard.dto.request.DangerAreaRequest;
import com.iroom.dashboard.dto.response.DangerAreaResponse;
import com.iroom.dashboard.entity.DangerArea;
import com.iroom.dashboard.repository.DangerAreaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DangerAreaServiceImpl implements DangerAreaService {

    private final DangerAreaRepository dangerAreaRepository;

    @Transactional
    @Override
    public DangerAreaResponse create(DangerAreaRequest request) {
        DangerArea dangerArea = DangerArea.builder()
                .blueprintId(request.blueprintId())
                .location(request.location())
                .width(request.width())
                .height(request.height())
                .build();
        return new DangerAreaResponse(dangerAreaRepository.save(dangerArea));
    }

    @Transactional
    @Override
    public DangerAreaResponse update(Long id, DangerAreaRequest request) {
        DangerArea dangerArea = dangerAreaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 위험구역이 존재하지 않습니다."));
        dangerArea.update(request.location(), request.width(), request.height());
        return new DangerAreaResponse(dangerArea);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        if (!dangerAreaRepository.existsById(id)) {
            throw new IllegalArgumentException("해당 위험구역이 존재하지 않습니다.");
        }
        dangerAreaRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public List<DangerAreaResponse> getAll() {
        return dangerAreaRepository.findAll()
                .stream()
                .map(DangerAreaResponse::new)
                .toList();
    }
}
