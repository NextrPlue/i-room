package com.iroom.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;


import com.iroom.dashboard.dto.response.DashBoardResponse;
import com.iroom.dashboard.entity.DashBoard;
import com.iroom.dashboard.repository.DashBoardRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DashBoardServiceTest {

	@Mock
	private DashBoardRepository dashBoardRepository;

	@InjectMocks
	private DashBoardService dashBoardService;

	private DashBoard dashBoard;

	@BeforeEach
	void setUp() {
		dashBoard = DashBoard.builder()
			.metricType("t")
			.metricValue(123)
			.recordedAt(LocalDate.now())
			.build();
	}

	@Test
	@DisplayName("getDashBoard - 성공")
	void getDashBoard_Success() {
		// given
		String metricType = "t";
		given(dashBoardRepository.findTopByMetricTypeOrderByIdDesc(metricType))
			.willReturn(dashBoard);

		// when
		DashBoardResponse result = dashBoardService.getDashBoard(metricType);

		// then
		assertThat(result.metricType()).isEqualTo("t");
		assertThat(result.metricValue()).isEqualTo(123);
		assertThat(result.recordedAt()).isEqualTo(dashBoard.getRecordedAt());
	}
}
