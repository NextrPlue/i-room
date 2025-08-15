package com.iroom.management.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.iroom.management.entity.WorkerManagement;

@RepositoryRestResource(exported = false)
public interface WorkerManagementRepository extends JpaRepository<WorkerManagement, Long> {
	Optional<WorkerManagement> findByWorkerId(Long workerId);

	Optional<WorkerManagement> findByWorkerIdAndOutDateIsNull(Long workerId);

	Optional<WorkerManagement> findTopByWorkerIdOrderByEnterDateDesc(Long workerId);
	
	Page<WorkerManagement> findByEnterDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
	
	List<WorkerManagement> findByEnterDateBetweenAndOutDateIsNull(LocalDateTime startDate, LocalDateTime endDate);
}
