package com.iroom.management.repository;


import com.iroom.management.entity.WorkerEdu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkerEduRepository extends JpaRepository<WorkerEdu, Long> {
    List<WorkerEdu> findByWorkerId(Long workerId);
}
