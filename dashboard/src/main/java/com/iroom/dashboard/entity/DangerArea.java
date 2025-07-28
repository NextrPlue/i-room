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
	private String location;

	@Column(nullable = false)
	private Double width;

	@Column(nullable = false)
	private Double height;

	@Builder
	public DangerArea(Long blueprintId, String location, Double width, Double height) {
		this.blueprintId = blueprintId;
		this.location = location;
		this.width = width;
		this.height = height;
	}

	public void update(String location, Double width, Double height) {
		this.location = location;
		this.width = width;
		this.height = height;
	}
}
