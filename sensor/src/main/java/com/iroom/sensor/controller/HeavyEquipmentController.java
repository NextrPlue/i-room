package com.iroom.sensor.controller;

import com.iroom.sensor.dto.HeavyEquipment.EquipmentUpdateLocationRequest;
import com.iroom.sensor.dto.HeavyEquipment.EquipmentUpdateLocationResponse;
import com.iroom.sensor.dto.HeavyEquipment.EquipmentRegisterRequest;
import com.iroom.sensor.dto.HeavyEquipment.EquipmentRegisterResponse;
import com.iroom.sensor.service.HeavyEquipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/heavy-equipments")
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
    public ResponseEntity<EquipmentUpdateLocationResponse> updateLocation(
            @RequestBody EquipmentUpdateLocationRequest request
    ){
        EquipmentUpdateLocationResponse response = heavyEquipmentService.updateLocation(request);
        return ResponseEntity.ok(response);
    }

}
