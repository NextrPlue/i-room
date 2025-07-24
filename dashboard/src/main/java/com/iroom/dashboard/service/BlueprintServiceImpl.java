package com.iroom.dashboard.service;

import com.iroom.dashboard.dto.request.BlueprintRequest;
import com.iroom.dashboard.dto.response.BlueprintResponse;
import com.iroom.dashboard.entity.Blueprint;
import com.iroom.dashboard.repository.BlueprintRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    public List<BlueprintResponse> getAllBlueprints() {
        return blueprintRepository.findAll()
                .stream()
                .map(BlueprintResponse::new)
                .toList();
    }
}
