package com.iroom.dashboard.dto.response;

import com.iroom.dashboard.entity.DangerArea;

public record DangerAreaResponse(
	Long id,
	Long blueprintId,
	String location,
	Double width,
	Double height
) {
	public DangerAreaResponse(DangerArea entity) {
		this(
			entity.getId(),
			entity.getBlueprintId(),
			entity.getLocation(),
			entity.getWidth(),
			entity.getHeight()
		);
	}
}
