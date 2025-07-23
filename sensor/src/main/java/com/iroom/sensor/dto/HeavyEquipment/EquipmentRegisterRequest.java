package com.iroom.sensor.dto.HeavyEquipment;

public record EquipmentRegisterRequest(
    String name,
    String type,
    Double radius
){}
