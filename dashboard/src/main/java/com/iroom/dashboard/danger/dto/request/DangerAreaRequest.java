package com.iroom.dashboard.danger.dto.request;

public record DangerAreaRequest(
	Long blueprintId,
	Double latitude,
	Double longitude,
	Double width,
	Double height,
	String name
) {
}
