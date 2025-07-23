package com.iroom.dashboard.repository;

import com.iroom.dashboard.entity.Blueprint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlueprintRepository extends JpaRepository<Blueprint, Long> {

}
