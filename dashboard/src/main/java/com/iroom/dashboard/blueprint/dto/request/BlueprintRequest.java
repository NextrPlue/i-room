package com.iroom.dashboard.blueprint.dto.request;

public record BlueprintRequest(
	String name,
	String blueprintUrl,
	Integer floor,
	Double width,
	Double height,
	GeoPointDto topLeft,
	GeoPointDto topRight,
	GeoPointDto bottomRight,
	GeoPointDto bottomLeft
) {
	public record GeoPointDto(Double lat, Double lon) {}
}