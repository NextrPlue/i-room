package com.iroom.user.repository;

import com.iroom.user.entity.Worker;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkerRepository extends JpaRepository<Worker, Long> {
    boolean existsByEmail(String email);
}
