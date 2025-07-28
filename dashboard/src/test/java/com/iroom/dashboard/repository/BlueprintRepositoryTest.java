package com.iroom.dashboard.repository;

import com.iroom.dashboard.entity.Blueprint;

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
			.blueprintUrl("https://example.com/blueprint1.png")
			.floor(1)
			.width(100.0)
			.height(100.0)
			.build();

		Blueprint blueprint2 = Blueprint.builder()
			.blueprintUrl("https://example.com/blueprint2.png")
			.floor(2)
			.width(200.0)
			.height(200.0)
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
}
