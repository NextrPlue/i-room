package com.iroom.sensor.repository;

import com.iroom.sensor.entity.HeavyEquipment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface HeavyEquipmentRepository extends JpaRepository<HeavyEquipment, Long> {
}
