package com.iroom.service;

import com.iroom.dto.EquipmentLocationUpdateRequest;
import com.iroom.dto.EquipmentLocationUpdateResponse;
import com.iroom.dto.EquipmentRegisterRequest;
import com.iroom.dto.EquipmentRegisterResponse;
import com.iroom.entity.HeavyEquipment;
import com.iroom.repository.HeavyEquipmentRepository;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class HeavyEquipmentService {

    private final HeavyEquipmentRepository heavyEquipmentRepository;

    //등록 기능
    public EquipmentRegisterResponse register(EquipmentRegisterRequest request){
        HeavyEquipment equipment = HeavyEquipment.builder()
                .name(request.name())
                .type(request.type())
                .radius(request.radius())
                .build();

        HeavyEquipment saved = heavyEquipmentRepository.save(equipment);
        return new EquipmentRegisterResponse(saved);
    }

    //위치 업데이트 기능
    public EquipmentLocationUpdateResponse updateLocation(EquipmentLocationUpdateRequest request){
        HeavyEquipment equipment = heavyEquipmentRepository.findById(request.id())
                .orElseThrow(() -> new EntityNotFoundException("장비 없음 "));

        equipment.updateLocation(request.location());

        return new EquipmentLocationUpdateResponse(equipment.getId(), equipment.getLocation());
    }
}
