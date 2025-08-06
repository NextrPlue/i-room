package com.iroom.sensor.service;

import com.iroom.modulecommon.exception.CustomException;
import com.iroom.modulecommon.exception.ErrorCode;
import com.iroom.modulecommon.service.KafkaProducerService;
import com.iroom.sensor.dto.HeavyEquipment.EquipmentUpdateLocationRequest;
import com.iroom.sensor.dto.HeavyEquipment.EquipmentUpdateLocationResponse;
import com.iroom.sensor.dto.HeavyEquipment.EquipmentRegisterRequest;
import com.iroom.sensor.dto.HeavyEquipment.EquipmentRegisterResponse;
import com.iroom.modulecommon.dto.event.EquipmentEvent;
import com.iroom.modulecommon.dto.event.EquipmentLocationEvent;
import com.iroom.sensor.entity.HeavyEquipment;
import com.iroom.sensor.repository.HeavyEquipmentRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class HeavyEquipmentService {

	private final HeavyEquipmentRepository heavyEquipmentRepository;
	private final KafkaProducerService kafkaProducerService;

	//등록 기능
	@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN')")
	public EquipmentRegisterResponse register(EquipmentRegisterRequest request) {
		if (request.radius() == null || request.radius() <= 0) {
			throw new CustomException(ErrorCode.SENSOR_INVALID_RADIUS);
		}

		HeavyEquipment equipment = HeavyEquipment.builder()
			.name(request.name())
			.type(request.type())
			.radius(request.radius())
			.build();

		HeavyEquipment saved = heavyEquipmentRepository.save(equipment);

		EquipmentEvent equipmentEvent = new EquipmentEvent(saved.getId(), saved.getName(), saved.getType(),
			saved.getRadius());

		kafkaProducerService.publishMessage("HEAVY_EQUIPMENT_REGISTERED", equipmentEvent);

		return new EquipmentRegisterResponse(saved);
	}

	//위치 업데이트 기능
	@PreAuthorize("hasAuthority('ROLE_EQUIPMENT_SYSTEM')")
	public EquipmentUpdateLocationResponse updateLocation(EquipmentUpdateLocationRequest request) {
		HeavyEquipment equipment = heavyEquipmentRepository.findById(request.id())
			.orElseThrow(() -> new CustomException(ErrorCode.SENSOR_EQUIPMENT_NOT_FOUND));

		// 위치 좌표 검증
		validateCoordinates(request.latitude(), request.longitude());

		equipment.updateLocation(request.latitude(), request.longitude());

		EquipmentLocationEvent equipmentLocationEvent = new EquipmentLocationEvent(
			equipment.getId(),
			equipment.getLatitude(),
			equipment.getLongitude(),
			equipment.getRadius()
		);

		kafkaProducerService.publishMessage("HEAVY_EQUIPMENT_LOCATION_UPDATED", equipmentLocationEvent);

		return new EquipmentUpdateLocationResponse(equipment.getId(), equipment.getLatitude(),
			equipment.getLongitude());
	}

	private void validateCoordinates(Double latitude, Double longitude) {
		if (latitude == null || longitude == null ||
			latitude < -90 || latitude > 90 ||
			longitude < -180 || longitude > 180) {
			throw new CustomException(ErrorCode.SENSOR_INVALID_COORDINATES);
		}
	}
}
