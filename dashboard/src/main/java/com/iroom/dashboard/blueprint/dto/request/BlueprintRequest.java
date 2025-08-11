package com.iroom.dashboard.blueprint.dto.request;

public record BlueprintRequest(
	String blueprintUrl,
	Integer floor,
	Double width,
	Double height
) {
}
