package com.iroom.modulecommon.dto.event;

public record EquipmentLocationEvent(
	Long equipmentId, //장비 id
	Double latitude, //위도
	Double longitude, //경도
	Double radius //반경
) {
}
