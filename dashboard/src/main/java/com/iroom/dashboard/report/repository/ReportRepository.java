package com.iroom.dashboard.report.repository;

import com.iroom.dashboard.report.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface ReportRepository extends JpaRepository<Report, Long> {
    
    Page<Report> findByReportType(Report.ReportType reportType, Pageable pageable);
    
    Page<Report> findByReportNameContaining(String reportName, Pageable pageable);
    
    Page<Report> findByPeriodContaining(String period, Pageable pageable);
}