package com.iroom.dashboard.report.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "report")
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String reportName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportType reportType;

    @Column(nullable = false)
    private String reportUrl;

    @Column(nullable = false)
    private String period; // 일일 리포트: "2024-01-15", 개선안 리포트: "2024-01-01~2024-01-31"

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    public Report(String reportName, ReportType reportType, String reportUrl, String period) {
        this.reportName = reportName;
        this.reportType = reportType;
        this.reportUrl = reportUrl;
        this.period = period;
    }

    public enum ReportType {
        DAILY_REPORT("일일 안전 리포트"),
        IMPROVEMENT_REPORT("개선안 리포트");

        private final String description;

        ReportType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}