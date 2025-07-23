package com.iroom.sensor.repository;

import com.iroom.sensor.entity.WorkerHealth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkerHealthRepository extends JpaRepository<WorkerHealth, Long> {
    Optional<WorkerHealth> findByWorkerId(Long workerId);
}
