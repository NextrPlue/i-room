package com.iroom.dashboard.controller;

import com.iroom.dashboard.dto.request.DangerAreaRequest;
import com.iroom.dashboard.dto.response.DangerAreaResponse;
import com.iroom.dashboard.service.DangerAreaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dangerAreas")
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
    public ResponseEntity<List<DangerAreaResponse>> getAll() {
        return ResponseEntity.ok(dangerAreaService.getAll());
    }
}
