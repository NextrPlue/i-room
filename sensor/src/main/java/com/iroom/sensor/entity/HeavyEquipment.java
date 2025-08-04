package com.iroom.sensor.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "HeavyEquipment_table")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HeavyEquipment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;
	private String type;

	@Column(nullable = true)
	private Double latitude;

	@Column(nullable = true)
	private Double longitude;

	private Double radius;

	@Builder
	public HeavyEquipment(String name, String type, Double radius) {
		this.name = name;
		this.type = type;
		this.radius = radius;
	}

	public void updateLocation(Double latitude, Double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

}