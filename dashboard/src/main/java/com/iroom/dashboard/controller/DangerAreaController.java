package com.iroom.dashboard.controller;

import com.iroom.dashboard.dto.request.DangerAreaRequest;
import com.iroom.dashboard.dto.response.DangerAreaResponse;
import com.iroom.dashboard.dto.response.PagedResponse;
import com.iroom.dashboard.service.DangerAreaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/danger-areas")
@RequiredArgsConstructor
public class DangerAreaController {
    private final DangerAreaService dangerAreaService;

    @PostMapping
    public ResponseEntity<DangerAreaResponse> create(@RequestBody DangerAreaRequest request) {
        return ResponseEntity.ok(dangerAreaService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DangerAreaResponse> update(@PathVariable Long id, @RequestBody DangerAreaRequest request) {
        return ResponseEntity.ok(dangerAreaService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        dangerAreaService.delete(id);
        return ResponseEntity.ok(Map.of("message", "위험구역 삭제 완료", "deletedId", id));
    }

    @GetMapping
    public ResponseEntity<PagedResponse<DangerAreaResponse>> getDangerAreas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        if (size > 50) size = 50;
        if (size < 0) size = 0;

        PagedResponse<DangerAreaResponse> response = dangerAreaService.getDangerAreas(page, size);
        return ResponseEntity.ok(response);
    }
}
