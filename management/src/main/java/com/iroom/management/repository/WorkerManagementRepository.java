package com.iroom.management.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iroom.management.entity.WorkerManagement;

@Repository
public interface WorkerManagementRepository extends JpaRepository<WorkerManagement, Long> {
	Optional<WorkerManagement> findByWorkerId(Long workerId);
}
