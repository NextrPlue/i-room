package com.iroom.dashboard.danger.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "DangerArea")
public class DangerArea {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long blueprintId;

	@Column(nullable = false)
	private Double latitude;

	@Column(nullable = false)
	private Double longitude;

	@Column(nullable = false)
	private Double width;

	@Column(nullable = false)
	private Double height;

	@Column(length = 100)
	private String name;

	@Builder
	public DangerArea(Long blueprintId, Double latitude, Double longitude, Double width, Double height, String name) {
		this.blueprintId = blueprintId;
		this.latitude = latitude;
		this.longitude = longitude;
		this.width = width;
		this.height = height;
		this.name = name;
	}

	public void update(Double latitude, Double longitude, Double width, Double height, String name) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.width = width;
		this.height = height;
		this.name = name;
	}
}
