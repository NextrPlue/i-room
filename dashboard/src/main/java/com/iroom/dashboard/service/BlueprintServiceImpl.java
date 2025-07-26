package com.iroom.dashboard.service;

import com.iroom.dashboard.dto.request.BlueprintRequest;
import com.iroom.dashboard.dto.response.BlueprintResponse;
import com.iroom.dashboard.dto.response.DangerAreaResponse;
import com.iroom.dashboard.dto.response.PagedResponse;
import com.iroom.dashboard.entity.Blueprint;
import com.iroom.dashboard.entity.DangerArea;
import com.iroom.dashboard.repository.BlueprintRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BlueprintServiceImpl implements BlueprintService {

    private final BlueprintRepository blueprintRepository;

    @Transactional
    @Override
    public BlueprintResponse createBlueprint(BlueprintRequest request) {
        Blueprint blueprint = Blueprint.builder()
                .blueprintUrl(request.blueprintUrl())
                .floor(request.floor())
                .width(request.width())
                .height(request.height())
                .build();
        return new BlueprintResponse(blueprintRepository.save(blueprint));
    }

    @Transactional
    @Override
    public BlueprintResponse updateBlueprint(Long id, BlueprintRequest request) {
        Blueprint blueprint = blueprintRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 도면이 존재하지 않습니다."));
        blueprint.update(request.blueprintUrl(), request.floor(), request.width(), request.height());
        return new BlueprintResponse(blueprint);
    }

    @Transactional
    @Override
    public void deleteBlueprint(Long id) {
        if (!blueprintRepository.existsById(id)) {
            throw new IllegalArgumentException("해당 도면이 존재하지 않습니다.");
        }
        blueprintRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public PagedResponse<BlueprintResponse> getAllBlueprints(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Blueprint> blueprints = blueprintRepository.findAll(pageable);
        Page<BlueprintResponse> responsePage = blueprints.map(BlueprintResponse::new);
        return PagedResponse.of(responsePage);
    }
}
