package com.iroom.dashboard.danger.dto.request;

public record RiskManagementRequest(
	String workerId,
	Double latitude,
	Double longitude
) {

}
