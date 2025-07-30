package com.iroom.management.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.iroom.management.entity.WorkerEdu;

@RepositoryRestResource(exported = false)
public interface WorkerEduRepository extends JpaRepository<WorkerEdu, Long> {
	Page<WorkerEdu> findAllByWorkerId(Long workerId, Pageable pageable);
}
