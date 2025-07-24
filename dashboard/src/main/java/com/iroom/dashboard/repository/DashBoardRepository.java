package com.iroom.dashboard.repository;

;


import com.iroom.dashboard.entity.DashBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface DashBoardRepository extends JpaRepository<DashBoard, Long> {
    DashBoard findTopByMetricTypeOrderByIdDesc(String metricType);

}
