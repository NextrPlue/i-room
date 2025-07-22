package com.iroom.management.repository;

import com.iroom.management.entity.WorkerManagement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkerManagementRepository extends JpaRepository<WorkerManagement, Long> {
    Optional<WorkerManagement> findByWorkerId(Long workerId);
}
