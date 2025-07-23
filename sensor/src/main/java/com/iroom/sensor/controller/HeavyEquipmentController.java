package com.iroom.sensor.controller;

import com.iroom.sensor.dto.HeavyEquipment.EquipmentLocationUpdateRequest;
import com.iroom.sensor.dto.HeavyEquipment.EquipmentLocationUpdateResponse;
import com.iroom.sensor.dto.HeavyEquipment.EquipmentRegisterRequest;
import com.iroom.sensor.dto.HeavyEquipment.EquipmentRegisterResponse;
import com.iroom.sensor.service.HeavyEquipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/heavyEquipments")
@RequiredArgsConstructor
public class HeavyEquipmentController {

    private final HeavyEquipmentService heavyEquipmentService;

    @PostMapping("/register")
    public ResponseEntity<EquipmentRegisterResponse> register(
            @RequestBody EquipmentRegisterRequest request
    ){
        EquipmentRegisterResponse response = heavyEquipmentService.register(request);
                return ResponseEntity.ok(response);
    }

    @PutMapping("/location")
    public ResponseEntity<EquipmentLocationUpdateResponse> updateLocation(
            @RequestBody EquipmentLocationUpdateRequest request
    ){
        EquipmentLocationUpdateResponse response = heavyEquipmentService.updateLocation(request);
        return ResponseEntity.ok(response);
    }

}
