package com.iroom.management.repository;


import com.iroom.management.entity.WorkerEdu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkerEduRepository extends JpaRepository<WorkerEdu, Long> {
    Page<WorkerEdu> findAllByWorkerId(Long workerId, Pageable pageable);
}
