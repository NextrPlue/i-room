package com.iroom.management.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.iroom.management.entity.WorkerManagement;

@RepositoryRestResource(exported = false)
public interface WorkerManagementRepository extends JpaRepository<WorkerManagement, Long> {
	Optional<WorkerManagement> findByWorkerId(Long workerId);
}
