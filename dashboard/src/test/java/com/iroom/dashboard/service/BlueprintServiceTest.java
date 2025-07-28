package com.iroom.dashboard.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;

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

import com.iroom.dashboard.dto.request.BlueprintRequest;
import com.iroom.dashboard.dto.response.BlueprintResponse;
import com.iroom.dashboard.entity.Blueprint;
import com.iroom.dashboard.repository.BlueprintRepository;

@ExtendWith(MockitoExtension.class)
class BlueprintServiceTest {

	@Mock
	private BlueprintRepository blueprintRepository;

	@InjectMocks
	private BlueprintService blueprintService;

	private Blueprint blueprint;
	private Pageable pageable;
	private Page<Blueprint> blueprintPage;

	@BeforeEach
	void setUp() {
		blueprint = Blueprint.builder()
			.blueprintUrl("url.png")
			.floor(1)
			.width(100.0)
			.height(200.0)
			.build();

		Blueprint blueprint2 = Blueprint.builder()
			.blueprintUrl("url2.png")
			.floor(2)
			.width(150.0)
			.height(250.0)
			.build();

		pageable = PageRequest.of(0, 10);
		blueprintPage = new PageImpl<>(List.of(blueprint, blueprint2), pageable, 2);
	}

	@Test
	@DisplayName("도면 생성 성공")
	void createBlueprintTest() {
		// given
		BlueprintRequest request = new BlueprintRequest("url.png", 1, 100.0, 200.0);
		given(blueprintRepository.save(any(Blueprint.class))).willReturn(blueprint);

		// when
		BlueprintResponse response = blueprintService.createBlueprint(request);

		// then
		assertThat(response.blueprintUrl()).isEqualTo("url.png");
		assertThat(response.floor()).isEqualTo(1);
	}
}