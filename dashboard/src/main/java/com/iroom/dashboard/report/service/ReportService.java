package com.iroom.dashboard.report.service;

import com.iroom.dashboard.report.dto.response.ReportResponse;
import com.iroom.dashboard.report.entity.Report;
import com.iroom.dashboard.report.repository.ReportRepository;
import com.iroom.modulecommon.dto.response.PagedResponse;
import com.iroom.modulecommon.exception.CustomException;
import com.iroom.modulecommon.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;

    @Value("${app.upload-dir}")
    private String uploadDir;

    // 리포트 저장
    public Report saveReport(byte[] pdfBytes, String reportName, Report.ReportType reportType, String period) {
        String storedFilename = generateReportFilename(reportName, reportType);
        String reportUrl = "/uploads/reports/" + storedFilename;
        
        saveReportFile(pdfBytes, storedFilename);

        Report report = Report.builder()
            .reportName(reportName)
            .reportType(reportType)
            .reportUrl(reportUrl)
            .period(period)
            .build();

        return reportRepository.save(report);
    }

    // 리포트 목록 조회
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_READER')")
    public PagedResponse<ReportResponse> getAllReports(String target, String keyword, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Report> reportPage;
        if (target == null || keyword == null || keyword.trim().isEmpty()) {
            reportPage = reportRepository.findAll(pageable);
        } else if ("type".equals(target)) {
            try {
                Report.ReportType reportType = Report.ReportType.valueOf(keyword.toUpperCase());
                reportPage = reportRepository.findByReportType(reportType, pageable);
            } catch (IllegalArgumentException e) {
                reportPage = reportRepository.findAll(pageable);
            }
        } else if ("name".equals(target)) {
            reportPage = reportRepository.findByReportNameContaining(keyword.trim(), pageable);
        } else if ("period".equals(target)) {
            reportPage = reportRepository.findByPeriodContaining(keyword.trim(), pageable);
        } else {
            reportPage = reportRepository.findAll(pageable);
        }

        Page<ReportResponse> responsePage = reportPage.map(ReportResponse::new);
        return PagedResponse.of(responsePage);
    }

    // 리포트 단일 조회
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_READER')")
    public ReportResponse getReport(Long id) {
        Report report = reportRepository.findById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.DASHBOARD_BLUEPRINT_NOT_FOUND));
        return new ReportResponse(report);
    }

    // 리포트 파일 다운로드
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_READER')")
    public Resource getReportFileResource(Long id) {
        Report report = reportRepository.findById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.DASHBOARD_BLUEPRINT_NOT_FOUND));

        try {
            String filename = report.getReportUrl().substring(report.getReportUrl().lastIndexOf("/") + 1);
            String rootPath = System.getProperty("user.dir");
            String filePath = Paths.get(rootPath, uploadDir, "reports", filename).toString();

            File file = new File(filePath);
            if (!file.exists()) {
                throw new CustomException(ErrorCode.DASHBOARD_BLUEPRINT_NOT_FOUND);
            }

            return new FileSystemResource(file);

        } catch (Exception e) {
            throw new CustomException(ErrorCode.DASHBOARD_BLUEPRINT_NOT_FOUND);
        }
    }

    // 리포트 삭제
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN')")
    public void deleteReport(Long id) {
        Report report = reportRepository.findById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.DASHBOARD_BLUEPRINT_NOT_FOUND));
        deleteReportFile(report.getReportUrl());
        reportRepository.deleteById(id);
    }

    private String generateReportFilename(String reportName, Report.ReportType reportType) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String typePrefix = reportType == Report.ReportType.DAILY_REPORT ? "daily" : "improvement";
        return String.format("%s_%s_%s_%s.pdf", typePrefix, reportName, date, UUID.randomUUID().toString().substring(0, 8));
    }

    private void saveReportFile(byte[] pdfBytes, String filename) {
        File directory = createReportDirectory();
        File destFile = new File(directory, filename);
        
        try {
            java.nio.file.Files.write(destFile.toPath(), pdfBytes);
        } catch (IOException e) {
            throw new RuntimeException("리포트 파일 저장 중 오류 발생", e);
        }
    }

    private File createReportDirectory() {
        String rootPath = System.getProperty("user.dir");
        String saveDirPath = Paths.get(rootPath, uploadDir, "reports").toString();

        File directory = new File(saveDirPath);
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new RuntimeException("리포트 디렉토리 생성에 실패했습니다: " + saveDirPath);
            }
        }
        return directory;
    }

    private void deleteReportFile(String reportUrl) {
        if (reportUrl != null && !reportUrl.isEmpty()) {
            try {
                String filename = reportUrl.substring(reportUrl.lastIndexOf("/") + 1);
                String rootPath = System.getProperty("user.dir");
                String filePath = Paths.get(rootPath, uploadDir, "reports", filename).toString();
                File file = new File(filePath);
                if (file.exists()) {
                    file.delete();
                }
            } catch (Exception e) {
                System.err.println("기존 리포트 파일 삭제 실패: " + e.getMessage());
            }
        }
    }
}