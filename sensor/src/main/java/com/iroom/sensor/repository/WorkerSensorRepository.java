package com.iroom.sensor.repository;

import com.iroom.sensor.entity.WorkerSensor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource(exported = false)
public interface WorkerSensorRepository extends JpaRepository<WorkerSensor, Long> {
	Optional<WorkerSensor> findByWorkerId(Long workerId);
	
	List<WorkerSensor> findByWorkerIdIn(List<Long> workerIds);
}
