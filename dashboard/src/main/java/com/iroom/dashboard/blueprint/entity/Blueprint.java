package com.iroom.dashboard.blueprint.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "Blueprint")
public class Blueprint {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String blueprintUrl;

	@Column(nullable = false)
	private Integer floor;

	@Column(nullable = false)
	private Double width;

	@Column(nullable = false)
	private Double height;

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "lat", column = @Column(name = "tl_lat", nullable = false)),
		@AttributeOverride(name = "lon", column = @Column(name = "tl_lon", nullable = false))
	})
	private GeoPoint topLeft;

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "lat", column = @Column(name = "tr_lat", nullable = false)),
		@AttributeOverride(name = "lon", column = @Column(name = "tr_lon", nullable = false))
	})
	private GeoPoint topRight;

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "lat", column = @Column(name = "br_lat", nullable = false)),
		@AttributeOverride(name = "lon", column = @Column(name = "br_lon", nullable = false))
	})
	private GeoPoint bottomRight;

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "lat", column = @Column(name = "bl_lat", nullable = false)),
		@AttributeOverride(name = "lon", column = @Column(name = "bl_lon", nullable = false))
	})
	private GeoPoint bottomLeft;

	@Builder
	public Blueprint(String name, String blueprintUrl, Integer floor, Double width, Double height,
		GeoPoint topLeft, GeoPoint topRight, GeoPoint bottomRight, GeoPoint bottomLeft) {
		this.name = name;
		this.blueprintUrl = blueprintUrl;
		this.floor = floor;
		this.width = width;
		this.height = height;
		this.topLeft = topLeft;
		this.topRight = topRight;
		this.bottomRight = bottomRight;
		this.bottomLeft = bottomLeft;
	}

	public void update(String name, String blueprintUrl, Integer floor, Double width, Double height,
		GeoPoint topLeft, GeoPoint topRight, GeoPoint bottomRight, GeoPoint bottomLeft) {
		this.name = name;
		this.blueprintUrl = blueprintUrl;
		this.floor = floor;
		this.width = width;
		this.height = height;
		this.topLeft = topLeft;
		this.topRight = topRight;
		this.bottomRight = bottomRight;
		this.bottomLeft = bottomLeft;
	}
}