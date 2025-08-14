package com.iroom.dashboard.blueprint.entity;

import jakarta.persistence.*;
import lombok.*;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GeoPoint {
	@Column(nullable = false)
	private Double lat;

	@Column(nullable = false)
	private Double lon;
}