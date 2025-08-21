package com.iroom.dashboard.repository;

import com.iroom.dashboard.blueprint.entity.Blueprint;
import com.iroom.dashboard.blueprint.entity.GeoPoint;
import com.iroom.dashboard.blueprint.repository.BlueprintRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class BlueprintRepositoryTest {

	@Autowired
	private BlueprintRepository blueprintRepository;

	private Pageable pageable;

	private Blueprint blueprint1;

	@BeforeEach
	void setUp() {
		blueprintRepository.deleteAll();

		blueprint1 = Blueprint.builder()
			.name("테스트 도면 1")
			.blueprintUrl("https://example.com/blueprint1.png")
			.floor(1)
			.width(100.0)
			.height(100.0)
			.topLeft(new GeoPoint(37.5665, 126.9780))
			.topRight(new GeoPoint(37.5665, 126.9790))
			.bottomRight(new GeoPoint(37.5655, 126.9790))
			.bottomLeft(new GeoPoint(37.5655, 126.9780))
			.build();

		Blueprint blueprint2 = Blueprint.builder()
			.name("테스트 도면 2")
			.blueprintUrl("https://example.com/blueprint2.png")
			.floor(2)
			.width(200.0)
			.height(200.0)
			.topLeft(new GeoPoint(37.5675, 126.9800))
			.topRight(new GeoPoint(37.5675, 126.9810))
			.bottomRight(new GeoPoint(37.5665, 126.9810))
			.bottomLeft(new GeoPoint(37.5665, 126.9800))
			.build();

		blueprintRepository.save(blueprint1);
		blueprintRepository.save(blueprint2);

		pageable = PageRequest.of(0, 10);
	}

	@Test
	@DisplayName("도면 저장 및 ID로 조회 성공")
	void saveAndFindById() {
		// when
		Optional<Blueprint> result = blueprintRepository.findById(blueprint1.getId());

		// then
		assertThat(result).isPresent();
		assertThat(result.get().getBlueprintUrl()).isEqualTo("https://example.com/blueprint1.png");
	}

	@Test
	@DisplayName("도면 전체 조회 성공")
	void findAllBlueprints() {
		// when
		Page<Blueprint> page = blueprintRepository.findAll(pageable);

		// then
		assertThat(page.getContent()).hasSize(2);
		assertThat(page.getTotalElements()).isEqualTo(2);
	}

	@Test
	@DisplayName("도면 삭제 성공")
	void deleteBlueprint() {
		// when
		blueprintRepository.delete(blueprint1);

		// then
		Optional<Blueprint> result = blueprintRepository.findById(blueprint1.getId());
		assertThat(result).isNotPresent();
	}

	@Test
	@DisplayName("층수별 도면 조회 성공")
	void findByFloor() {
		// when
		Page<Blueprint> page = blueprintRepository.findByFloor(1, pageable);

		// then
		assertThat(page.getContent()).hasSize(1);
		assertThat(page.getContent().get(0).getFloor()).isEqualTo(1);
		assertThat(page.getContent().get(0).getName()).isEqualTo("테스트 도면 1");
	}

	@Test
	@DisplayName("이름별 도면 조회 성공")
	void findByName() {
		// when
		Page<Blueprint> page = blueprintRepository.findByName("테스트 도면 2", pageable);

		// then
		assertThat(page.getContent()).hasSize(1);
		assertThat(page.getContent().get(0).getName()).isEqualTo("테스트 도면 2");
		assertThat(page.getContent().get(0).getFloor()).isEqualTo(2);
	}
}
