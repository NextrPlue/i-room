package com.iroom.dashboard.report.dto.response;

import com.iroom.dashboard.report.entity.Report;

import java.time.LocalDateTime;

public record ReportResponse(
    Long id,
    String reportName,
    Report.ReportType reportType,
    String reportUrl,
    String period,
    LocalDateTime createdAt
) {
    public ReportResponse(Report report) {
        this(
            report.getId(),
            report.getReportName(),
            report.getReportType(),
            report.getReportUrl(),
            report.getPeriod(),
            report.getCreatedAt()
        );
    }
}