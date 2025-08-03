package com.iroom.sensor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.iroom.sensor.entity.WorkerReadModel;

@RepositoryRestResource(exported = false)
public interface WorkerReadModelRepository extends JpaRepository<WorkerReadModel, Long> {
}