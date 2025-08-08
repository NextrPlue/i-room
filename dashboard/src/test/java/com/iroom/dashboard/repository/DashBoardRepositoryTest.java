package com.iroom.dashboard.repository;

import com.iroom.dashboard.entity.DashBoard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DashBoardRepositoryTest {

	@Autowired
	private DashBoardRepository dashBoardRepository;

	private DashBoard dashBoard1;
	private DashBoard dashBoard2;

	@BeforeEach
	void setUp() {
		dashBoard1 = DashBoard.builder()
			.metricType("t")
			.metricValue(100)
			.recordedAt(LocalDate.now().minusDays(1))
			.build();
		dashBoardRepository.save(dashBoard1);

		dashBoard2 = DashBoard.builder()
			.metricType("t")
			.metricValue(200)
			.recordedAt(LocalDate.now())
			.build();
		dashBoardRepository.save(dashBoard2);
	}

	@Test
	@DisplayName("findTopByMetricTypeOrderByIdDesc - 성공")
	void findTopByMetricTypeOrderByIdDesc_Success() {
		// when
		DashBoard foundDashBoard = dashBoardRepository.findTopByMetricTypeOrderByIdDesc("t");

		// then
		assertNotNull(foundDashBoard);
		assertEquals(dashBoard2.getMetricValue(), foundDashBoard.getMetricValue());
		assertEquals(dashBoard2.getId(), foundDashBoard.getId());
	}
}