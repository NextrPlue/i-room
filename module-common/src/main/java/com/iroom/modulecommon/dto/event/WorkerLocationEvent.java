package com.iroom.modulecommon.dto.event;

public record WorkerLocationEvent(
	Long workerId,
	Double latitude, //위도
	Double longitude //경도
) {

}
