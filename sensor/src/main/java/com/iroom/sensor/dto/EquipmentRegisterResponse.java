package com.iroom.sensor.dto;


import com.iroom.sensor.entity.HeavyEquipment;

public record EquipmentRegisterResponse(
    Long id,
    String name,
    String type,
    double radius
) {
    public EquipmentRegisterResponse(HeavyEquipment equipment){
        this(
                equipment.getId(),
                equipment.getName(),
                equipment.getType(),
                equipment.getRadius()
        );
    }
}
