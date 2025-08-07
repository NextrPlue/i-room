package com.iroom.dashboard.dto.request;

public record RiskManagementRequest(
	String workerId,
	Double latitude,
	Double longitude
) {

}
