package com.iroom.dashboard.blueprint.dto.response;

import com.iroom.dashboard.blueprint.dto.request.BlueprintRequest;
import com.iroom.dashboard.blueprint.entity.Blueprint;

public record BlueprintResponse(
	Long id,
	String name,
	String blueprintUrl,
	Integer floor,
	Double width,
	Double height,
	BlueprintRequest.GeoPointDto topLeft,
	BlueprintRequest.GeoPointDto topRight,
	BlueprintRequest.GeoPointDto bottomRight,
	BlueprintRequest.GeoPointDto bottomLeft
) {
	public BlueprintResponse(Blueprint b) {
		this(
			b.getId(),
			b.getName(),
			b.getBlueprintUrl(),
			b.getFloor(),
			b.getWidth(),
			b.getHeight(),
			new BlueprintRequest.GeoPointDto(b.getTopLeft().getLat(), b.getTopLeft().getLon()),
			new BlueprintRequest.GeoPointDto(b.getTopRight().getLat(), b.getTopRight().getLon()),
			new BlueprintRequest.GeoPointDto(b.getBottomRight().getLat(), b.getBottomRight().getLon()),
			new BlueprintRequest.GeoPointDto(b.getBottomLeft().getLat(), b.getBottomLeft().getLon())
		);
	}
}
