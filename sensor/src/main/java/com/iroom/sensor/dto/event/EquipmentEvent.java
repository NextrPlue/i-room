package com.iroom.sensor.dto.event;

public record EquipmentEvent(
	Long id,
	String name,
	String type,
	Double radius
) {
}
