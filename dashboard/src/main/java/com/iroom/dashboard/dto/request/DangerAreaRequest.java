package com.iroom.dashboard.dto.request;

public record DangerAreaRequest(
	Long blueprintId,
	String location,
	Double width,
	Double height
) {
}
