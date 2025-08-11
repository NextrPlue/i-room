package com.iroom.dashboard.blueprint.dto.response;

import com.iroom.dashboard.blueprint.entity.Blueprint;

public record BlueprintResponse(
	Long id,
	String blueprintUrl,
	Integer floor,
	Double width,
	Double height
) {
	// 엔티티 -> DTO로 변환하는 생성자
	public BlueprintResponse(Blueprint blueprint) {
		this(
			blueprint.getId(),
			blueprint.getBlueprintUrl(),
			blueprint.getFloor(),
			blueprint.getWidth(),
			blueprint.getHeight()
		);
	}
}
