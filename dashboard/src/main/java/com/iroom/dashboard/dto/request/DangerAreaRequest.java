package com.iroom.dashboard.dto.request;

public record DangerAreaRequest(
	Long blueprintId,
	Double latitude,
	Double longitude,
	Double width,
	Double height
) {
}
