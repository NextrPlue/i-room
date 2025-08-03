package com.iroom.modulecommon.dto.event;

import com.iroom.sensor.entity.WorkerHealth;

public record WorkerLocationEvent(
	Long workerId,
	Double latitude, //위도
	Double longitude //경도
) {

}
