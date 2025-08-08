package com.iroom.dashboard.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.iroom.dashboard.entity.DashBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface DashBoardRepository extends JpaRepository<DashBoard, Long> {
    DashBoard findTopByMetricTypeOrderByIdDesc(String metricType);


    Optional<DashBoard> findByMetricTypeAndRecordedAt(String metricType, LocalDate recordedAt);

    @Query(value = """
    SELECT
      DATE(recorded_at) AS day,
      metric_type,
      SUM(metric_value) AS total_value
    FROM dash_board
    GROUP BY day, metric_type
    ORDER BY day DESC, metric_type
    """, nativeQuery = true)
    List<Object[]> getDailyMetricSummaryRaw();


    @Query(value = """
    WITH latest_date AS (
      SELECT MAX(recorded_at) AS max_date FROM dash_board
    ),
    week_data AS (
      SELECT 
        metric_type,
        metric_value,
        recorded_at,
        FLOOR(DATEDIFF((SELECT max_date FROM latest_date), recorded_at) / 7) AS week_number
      FROM dash_board
    )
    SELECT
      DATE_SUB((SELECT max_date FROM latest_date), INTERVAL week_number * 7 DAY) AS week_start,
      metric_type,
      SUM(metric_value) AS total_value
    FROM week_data
    GROUP BY week_start, metric_type
    ORDER BY week_start DESC, metric_type
    """, nativeQuery = true)
    List<Object[]> getWeeklyMetricSummaryRaw();



    @Query(value = """
    WITH latest_date AS (
      SELECT MAX(recorded_at) AS max_date FROM dash_board
    ),
    month_data AS (
      SELECT 
        metric_type,
        metric_value,
        recorded_at,
        FLOOR(DATEDIFF((SELECT max_date FROM latest_date), recorded_at) / 30) AS month_number
      FROM dash_board
    )
    SELECT
      DATE_SUB((SELECT max_date FROM latest_date), INTERVAL month_number * 30 DAY) AS month_start,
      metric_type,
      SUM(metric_value) AS total_value
    FROM month_data
    GROUP BY month_start, metric_type
    ORDER BY month_start DESC, metric_type
    """, nativeQuery = true)
    List<Object[]> getMonthlyMetricSummaryRaw();



}
