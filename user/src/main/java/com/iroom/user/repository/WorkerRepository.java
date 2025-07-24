package com.iroom.user.repository;

import com.iroom.user.entity.Worker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;

@RepositoryRestResource(exported = false)
public interface WorkerRepository extends JpaRepository<Worker, Long> {
    boolean existsByEmail(String email);
    Optional<Worker> findByEmail(String email);
}
