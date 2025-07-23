package com.iroom.sensor.service;

import com.iroom.sensor.dto.EquipmentRegisterRequest;
import com.iroom.sensor.dto.EquipmentRegisterResponse;
import com.iroom.sensor.entity.HeavyEquipment;
import com.iroom.sensor.repository.HeavyEquipmentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

}
