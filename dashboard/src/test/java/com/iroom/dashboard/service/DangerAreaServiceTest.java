package com.iroom.dashboard.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import com.iroom.dashboard.danger.service.DangerAreaService;
import com.iroom.dashboard.danger.dto.request.DangerAreaRequest;
import com.iroom.dashboard.danger.dto.response.DangerAreaResponse;
import com.iroom.dashboard.danger.entity.DangerArea;
import com.iroom.dashboard.danger.repository.DangerAreaRepository;
import com.iroom.modulecommon.dto.response.PagedResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class DangerAreaServiceTest {

	@Mock
	private DangerAreaRepository dangerAreaRepository;

	@InjectMocks
	private DangerAreaService dangerAreaService;

	private DangerArea dangerArea;
	private Pageable pageable;
	private Page<DangerArea> dangerAreaPage;

	@BeforeEach
	void setUp() {
		dangerArea = DangerArea.builder()
			.blueprintId(1L)
			.latitude(10.0)
			.longitude(20.0)
			.width(100.0)
			.height(200.0)
			.build();

		DangerArea dangerArea2 = DangerArea.builder()
			.blueprintId(2L)
			.latitude(50.0)
			.longitude(60.0)
			.width(150.0)
			.height(250.0)
			.build();

		pageable = PageRequest.of(0, 10);
		dangerAreaPage = new PageImpl<>(List.of(dangerArea, dangerArea2), pageable, 2);
	}

	@Test
	@DisplayName("위험구역 등록 성공")
	void createDangerAreaTest() {
		// given
		DangerAreaRequest request = new DangerAreaRequest(1L, 10.0, 20.0, 100.0, 200.0);
		given(dangerAreaRepository.save(any(DangerArea.class))).willReturn(dangerArea);

		// when
		DangerAreaResponse response = dangerAreaService.createDangerArea(request);

		// then
		assertThat(response.blueprintId()).isEqualTo(1L);
		assertThat(response.latitude()).isEqualTo(10.0);
		assertThat(response.longitude()).isEqualTo(20.0);
	}

	@Test
	@DisplayName("위험구역 수정 성공")
	void updateDangerAreaTest() {
		// given
		Long id = 1L;
		DangerAreaRequest request = new DangerAreaRequest(1L, 99.0, 88.0, 300.0, 400.0);
		given(dangerAreaRepository.findById(id)).willReturn(Optional.of(dangerArea));

		// when
		DangerAreaResponse response = dangerAreaService.updateDangerArea(id, request);

		// then
		assertThat(response.latitude()).isEqualTo(99.0);
		assertThat(response.longitude()).isEqualTo(88.0);
		assertThat(response.width()).isEqualTo(300.0);
		assertThat(response.height()).isEqualTo(400.0);
	}

	@Test
	@DisplayName("위험구역 수정 실패 - 존재하지 않음")
	void updateDangerAreaFail_NotFound() {
		// given
		Long id = 999L;
		DangerAreaRequest request = new DangerAreaRequest(1L, 10.0, 10.0, 100.0, 100.0);
		given(dangerAreaRepository.findById(id)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> dangerAreaService.updateDangerArea(id, request))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("해당 위험구역이 존재하지 않습니다.");
	}

	@Test
	@DisplayName("위험구역 삭제 성공")
	void deleteDangerAreaTest() {
		// given
		Long id = 1L;
		given(dangerAreaRepository.existsById(id)).willReturn(true);

		// when
		dangerAreaService.deleteDangerArea(id);

		// then
		verify(dangerAreaRepository).deleteById(id);
	}

	@Test
	@DisplayName("위험구역 삭제 실패 - 존재하지 않음")
	void deleteDangerAreaFail_NotFound() {
		// given
		Long id = 999L;
		given(dangerAreaRepository.existsById(id)).willReturn(false);

		// when & then
		assertThatThrownBy(() -> dangerAreaService.deleteDangerArea(id))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("해당 위험구역이 존재하지 않습니다.");
	}

	@Test
	@DisplayName("위험구역 전체 조회 성공")
	void getAllDangerAreasTest() {
		// given
		given(dangerAreaRepository.findAll(pageable)).willReturn(dangerAreaPage);

		// when
		PagedResponse<DangerAreaResponse> response = dangerAreaService.getAllDangerAreas(0, 10);

		// then
		assertThat(response.content()).hasSize(2);
		assertThat(response.totalElements()).isEqualTo(2);
		assertThat(response.content().get(0).latitude()).isEqualTo(10.0);
		assertThat(response.content().get(0).longitude()).isEqualTo(20.0);
	}
}