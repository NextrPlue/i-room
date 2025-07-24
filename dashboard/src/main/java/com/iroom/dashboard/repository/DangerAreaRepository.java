package com.iroom.dashboard.repository;

import com.iroom.dashboard.entity.DangerArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DangerAreaRepository extends JpaRepository<DangerArea, Long> {
}
