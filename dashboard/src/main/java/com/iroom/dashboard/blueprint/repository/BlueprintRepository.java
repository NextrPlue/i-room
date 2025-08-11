package com.iroom.dashboard.blueprint.repository;

import com.iroom.dashboard.blueprint.entity.Blueprint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface BlueprintRepository extends JpaRepository<Blueprint, Long> {

}
