package com.iroom.dashboard.dto.response;

import com.iroom.dashboard.entity.DangerArea;

public record DangerAreaResponse(
	Long id,
	Long blueprintId,
	String latitude,
	String longitude,
	Double width,
	Double height
) {
	public DangerAreaResponse(DangerArea entity) {
		this(
			entity.getId(),
			entity.getBlueprintId(),
			entity.getLatitude(),
			entity.getLongitude(),
			entity.getWidth(),
			entity.getHeight()
		);
	}
}
