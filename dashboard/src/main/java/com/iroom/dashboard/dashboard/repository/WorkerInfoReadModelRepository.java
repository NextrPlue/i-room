package com.iroom.dashboard.dashboard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.iroom.dashboard.dashboard.entity.WorkerInfoReadModel;

@RepositoryRestResource(exported = false)
public interface WorkerInfoReadModelRepository extends JpaRepository<WorkerInfoReadModel,Long> {

}
