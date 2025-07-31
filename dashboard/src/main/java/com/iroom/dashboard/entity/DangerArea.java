package com.iroom.dashboard.entity;

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
	private String latitude;

	@Column(nullable = false)
	private String longitude;

	@Column(nullable = false)
	private Double width;

	@Column(nullable = false)
	private Double height;

	@Builder
	public DangerArea(Long blueprintId, String latitude, String longitude, Double width, Double height) {
		this.blueprintId = blueprintId;
		this.latitude = latitude;
		this.longitude = longitude;
		this.width = width;
		this.height = height;
	}

	public void update(String latitude, String longitude, Double width, Double height) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.width = width;
		this.height = height;
	}
}
