package com.iroom.sensor.dto;

public record EquipmentRegisterRequest(
    String name,
    String type,
    double radius
){}
