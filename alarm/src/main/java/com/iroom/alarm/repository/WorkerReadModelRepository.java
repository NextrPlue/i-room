package com.iroom.alarm.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.iroom.alarm.entity.WorkerReadModel;

@RepositoryRestResource(exported = false)
public interface WorkerReadModelRepository extends JpaRepository<WorkerReadModel, Long> {

	Page<WorkerReadModel> findByNameContaining(String name, Pageable pageable);

	Page<WorkerReadModel> findByEmailContaining(String email, Pageable pageable);

	Page<WorkerReadModel> findByDepartmentContaining(String department, Pageable pageable);
}