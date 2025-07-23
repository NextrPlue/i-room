package com.iroom.sensor.controller;

import com.iroom.sensor.dto.EquipmentRegisterRequest;
import com.iroom.sensor.dto.EquipmentRegisterResponse;
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

}
