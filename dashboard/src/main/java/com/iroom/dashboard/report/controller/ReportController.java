package com.iroom.dashboard.report.controller;

import com.iroom.dashboard.report.dto.response.ReportResponse;
import com.iroom.dashboard.report.service.ReportService;
import com.iroom.modulecommon.dto.response.ApiResponse;
import com.iroom.modulecommon.dto.response.PagedResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // 리포트 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<ReportResponse>>> getAllReports(
        @RequestParam(required = false) String target,
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "0") Integer page,
        @RequestParam(defaultValue = "10") Integer size
    ) {
        if (size > 50) {
            size = 50;
        }
        if (size < 0) {
            size = 0;
        }

        PagedResponse<ReportResponse> responses = reportService.getAllReports(target, keyword, page, size);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    // 리포트 단일 조회
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReportResponse>> getReport(@PathVariable Long id) {
        ReportResponse response = reportService.getReport(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 리포트 파일 다운로드
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadReport(@PathVariable Long id) {
        Resource resource = reportService.getReportFileResource(id);
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment")
            .contentType(MediaType.APPLICATION_PDF)
            .body(resource);
    }

    // 리포트 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteReport(@PathVariable Long id) {
        reportService.deleteReport(id);
        Map<String, Object> result = Map.of("message", "리포트 삭제 완료", "deletedId", id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}