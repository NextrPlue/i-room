package com.iroom.management.repository;

import com.iroom.management.entity.WorkerReadModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkerReadModelRepository extends JpaRepository<WorkerReadModel, Long> {
    
    Page<WorkerReadModel> findByNameContaining(String name, Pageable pageable);
    
    Page<WorkerReadModel> findByEmailContaining(String email, Pageable pageable);
    
    Page<WorkerReadModel> findByDepartmentContaining(String department, Pageable pageable);
}