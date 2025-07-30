package com.iroom.user.system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;

@RepositoryRestResource(exported = false)
public interface SystemRepository extends JpaRepository<System, Long> {
    Optional<System> findByApiKey(String apiKey);
    Optional<System> findByName(String name);
}
