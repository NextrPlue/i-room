package com.iroom.sensor.service;

import com.iroom.sensor.dto.WorkerHealth.WorkerUpdateLocationRequest;
import com.iroom.sensor.dto.WorkerHealth.WorkerUpdateLocationResponse;
import com.iroom.sensor.dto.WorkerHealth.WorkerUpdateVitalSignsRequest;
import com.iroom.sensor.dto.WorkerHealth.WorkerUpdateVitalSignsResponse;
import com.iroom.sensor.entity.WorkerHealth;
import com.iroom.sensor.repository.WorkerHealthRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class WorkerHealthService {

    private final WorkerHealthRepository repository;

    //위치 업데이트 기능
    public WorkerUpdateLocationResponse updateLocation(WorkerUpdateLocationRequest request){
        WorkerHealth health = repository.findByWorkerId(request.workerId())
                .orElseThrow(() -> new EntityNotFoundException("해당 근로자 없음"));

        health.updateLocation(request.location());

        return new WorkerUpdateLocationResponse(health.getWorkerId(), health.getWorkerLocation());
    }

    //생체정보 업데이트 기능
    public WorkerUpdateVitalSignsResponse updateVitalSigns(WorkerUpdateVitalSignsRequest request){
        WorkerHealth health = repository.findByWorkerId(request.workerId())
                .orElseThrow(() -> new EntityNotFoundException("해당 근로자 없음"));

        health.updateVitalSign(request.heartRate(), request.bodyTemperature());

        return new WorkerUpdateVitalSignsResponse(
                health.getWorkerId(),
                health.getHeartRate(),
                health.getBodyTemperature()
        );
    }

    //위치 조회 기능
    public WorkerUpdateLocationResponse getWorkerLocation(Long workerId){
        WorkerHealth health = repository.findByWorkerId(workerId)
                .orElseThrow(() -> new EntityNotFoundException("해당 근로자 없음"));

        return new WorkerUpdateLocationResponse(health);
    }
}
