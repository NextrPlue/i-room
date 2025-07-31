package com.iroom.sensor.dto.HeavyEquipment;

public record EquipmentUpdateLocationRequest(
	Long id,
	Double latitude,
	Double longitude
) {
}
