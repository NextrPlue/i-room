package com.iroom.dashboard.danger.repository;

import com.iroom.dashboard.danger.entity.DangerArea;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface DangerAreaRepository extends JpaRepository<DangerArea, Long> {
}
