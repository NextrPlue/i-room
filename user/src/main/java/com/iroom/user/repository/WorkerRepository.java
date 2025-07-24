package com.iroom.user.repository;

import com.iroom.user.entity.Worker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkerRepository extends JpaRepository<Worker, Long> {
    boolean existsByEmail(String email);
    Optional<Worker> findByEmail(String email);
}
