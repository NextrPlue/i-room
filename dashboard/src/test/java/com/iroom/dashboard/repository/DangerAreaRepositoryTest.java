package com.iroom.dashboard.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.iroom.dashboard.entity.DangerArea;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class DangerAreaRepositoryTest {

	@Autowired
	private DangerAreaRepository dangerAreaRepository;

	private Pageable pageable;
	private DangerArea dangerArea1;

	@BeforeEach
	void setUp() {
		dangerAreaRepository.deleteAll();

		dangerArea1 = DangerArea.builder()
			.blueprintId(1L)
			.location("10,20")
			.width(30.0)
			.height(40.0)
			.build();

		DangerArea dangerArea2 = DangerArea.builder()
			.blueprintId(1L)
			.location("50,60")
			.width(25.0)
			.height(35.0)
			.build();

		dangerAreaRepository.save(dangerArea1);
		dangerAreaRepository.save(dangerArea2);

		pageable = PageRequest.of(0, 10);
	}

	@Test
	@DisplayName("위험구역 저장 및 조회 성공")
	void saveAndFindById() {
		// when
		Optional<DangerArea> result = dangerAreaRepository.findById(dangerArea1.getId());

		// then
		assertThat(result).isPresent();
		assertThat(result.get().getLocation()).isEqualTo("10,20");
	}

	@Test
	@DisplayName("위험구역 전체 조회 성공")
	void findAllDangerAreas() {
		// when
		Page<DangerArea> page = dangerAreaRepository.findAll(pageable);

		// then
		assertThat(page.getContent()).hasSize(2);
		assertThat(page.getTotalElements()).isEqualTo(2);
	}

	@Test
	@DisplayName("위험구역 삭제 성공")
	void deleteDangerArea() {
		// when
		dangerAreaRepository.delete(dangerArea1);

		// then
		Optional<DangerArea> result = dangerAreaRepository.findById(dangerArea1.getId());
		assertThat(result).isNotPresent();
	}
}