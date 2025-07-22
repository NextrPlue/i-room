package com.iroom.controller;

import com.iroom.dto.EquipmentLocationUpdateRequest;
import com.iroom.dto.EquipmentLocationUpdateResponse;
import com.iroom.dto.EquipmentRegisterRequest;
import com.iroom.dto.EquipmentRegisterResponse;
import com.iroom.service.HeavyEquipmentService;
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
