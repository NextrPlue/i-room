package com.iroom.sensor.repository;

import com.iroom.sensor.entity.HeavyEquipment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class EquipmentRepositoryTest {

	@Autowired
	private HeavyEquipmentRepository equipmentRepository;

	@Test
	@DisplayName("장비 저장 후 ID로 조회 테스트")
	void saveAndFindById() {
		// given
		HeavyEquipment equipment = HeavyEquipment.builder()
			.name("포크레인A-1")
			.type("포크레인")
			.radius(10.0)
			.build();

		// when
		HeavyEquipment saved = equipmentRepository.save(equipment);
		Optional<HeavyEquipment> found = equipmentRepository.findById(saved.getId());

		// then
		assertThat(found).isPresent();
		assertThat(found.get().getName()).isEqualTo("포크레인A-1");
		assertThat(found.get().getType()).isEqualTo("포크레인");
		assertThat(found.get().getRadius()).isEqualTo(10.0);

	}

	@Test
	@DisplayName("updateLocation 메서드 위치 수정 테스트")
	void updateLocationTest() {
		// given
		HeavyEquipment equipment = HeavyEquipment.builder()
			.name("크레인B-3")
			.type("크레인")
			.radius(13.0)
			.build();
		HeavyEquipment saved = equipmentRepository.save(equipment);

		// when
		Double latitude = 35.8343;
		Double longitude = 128.4723;
		saved.updateLocation(latitude, longitude);
		HeavyEquipment updated = equipmentRepository.save(saved);

		// then
		Optional<HeavyEquipment> result = equipmentRepository.findById(updated.getId());
		assertThat(result).isPresent();
		assertThat(result.get().getLatitude()).isEqualTo(latitude);
		assertThat(result.get().getLongitude()).isEqualTo(longitude);
	}
}
