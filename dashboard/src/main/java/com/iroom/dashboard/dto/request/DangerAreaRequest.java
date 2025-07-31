package com.iroom.dashboard.dto.request;

public record DangerAreaRequest(
	Long blueprintId,
	String latitude,
	String longitude,
	Double width,
	Double height
) {
}
