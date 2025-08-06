package com.iroom.dashboard.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import com.iroom.dashboard.dto.request.BlueprintRequest;
import com.iroom.dashboard.dto.response.BlueprintResponse;
import com.iroom.dashboard.entity.Blueprint;
import com.iroom.dashboard.repository.BlueprintRepository;
import com.iroom.modulecommon.dto.response.PagedResponse;

@ExtendWith(MockitoExtension.class)
class BlueprintServiceTest {

	@Mock
	private BlueprintRepository blueprintRepository;

	@InjectMocks
	private BlueprintService blueprintService;

	private Blueprint blueprint;
	private Pageable pageable;
	private Page<Blueprint> blueprintPage;
	private MultipartFile validFile;
	private MultipartFile invalidFile;
	private MultipartFile nullNameFile;

	@BeforeEach
	void setUp() {
		blueprint = Blueprint.builder()
			.blueprintUrl("/uploads/blueprints/test-uuid.png")
			.floor(1)
			.width(100.0)
			.height(200.0)
			.build();

		Blueprint blueprint2 = Blueprint.builder()
			.blueprintUrl("/uploads/blueprints/test-uuid2.png")
			.floor(2)
			.width(150.0)
			.height(250.0)
			.build();

		pageable = PageRequest.of(0, 10);
		blueprintPage = new PageImpl<>(List.of(blueprint, blueprint2), pageable, 2);
		
		// 테스트용 파일 생성
		validFile = new MockMultipartFile(
			"file",
			"test-blueprint.png",
			"image/png",
			"test image content".getBytes()
		);
		
		invalidFile = new MockMultipartFile(
			"file",
			"test-blueprint", // 확장자 없음
			"text/plain",
			"test content".getBytes()
		);
		
		nullNameFile = new MockMultipartFile(
			"file",
			null, // null 파일명
			"image/png",
			"test content".getBytes()
		);
		
		// upload-dir 설정
		ReflectionTestUtils.setField(blueprintService, "uploadDir", "uploads/blueprints");
	}

	@Test
	@DisplayName("도면 생성 성공")
	void createBlueprintTest() {
		// given
		BlueprintRequest request = new BlueprintRequest(null, 1, 100.0, 200.0);
		given(blueprintRepository.save(any(Blueprint.class))).willReturn(blueprint);

		// when
		BlueprintResponse response = blueprintService.createBlueprint(validFile, request);

		// then
		assertThat(response.blueprintUrl()).contains("/uploads/blueprints/");
		assertThat(response.blueprintUrl()).contains(".png");
		assertThat(response.floor()).isEqualTo(1);
		assertThat(response.width()).isEqualTo(100.0);
		assertThat(response.height()).isEqualTo(200.0);
		verify(blueprintRepository).save(any(Blueprint.class));
	}
	
	@Test
	@DisplayName("도면 생성 실패 - 파일명이 null")
	void createBlueprintFailNullFilename() {
		// given
		BlueprintRequest request = new BlueprintRequest(null, 1, 100.0, 200.0);

		// when & then
		assertThatThrownBy(() -> blueprintService.createBlueprint(nullNameFile, request))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("파일명이 유효하지 않습니다.");
	}
	
	@Test
	@DisplayName("도면 생성 실패 - 파일 확장자 없음")
	void createBlueprintFailNoExtension() {
		// given
		BlueprintRequest request = new BlueprintRequest(null, 1, 100.0, 200.0);

		// when & then
		assertThatThrownBy(() -> blueprintService.createBlueprint(invalidFile, request))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("파일 확장자가 없습니다.");
	}
	
	@Test
	@DisplayName("도면 생성 실패 - 디렉토리 생성 실패")
	void createBlueprintFailDirectoryCreation() {
		// given
		BlueprintRequest request = new BlueprintRequest(null, 1, 100.0, 200.0);
		
		// 디렉토리 생성이 실패하는 시나리오를 위해 읽기 전용 경로 설정
		ReflectionTestUtils.setField(blueprintService, "uploadDir", "/invalid/readonly/path");

		// when & then
		assertThatThrownBy(() -> blueprintService.createBlueprint(validFile, request))
			.isInstanceOf(RuntimeException.class)
			.hasMessage("디렉토리 생성 실패");
	}

	@Test
	@DisplayName("도면 수정 성공")
	void updateBlueprintTest() {
		// given
		Long id = 1L;
		BlueprintRequest request = new BlueprintRequest("new_url.png", 1, 120.0, 220.0);
		given(blueprintRepository.findById(id)).willReturn(Optional.of(blueprint));

		// when
		BlueprintResponse response = blueprintService.updateBlueprint(id, request);

		// then
		assertThat(response.blueprintUrl()).isEqualTo("new_url.png");
		assertThat(response.width()).isEqualTo(120);
		assertThat(response.height()).isEqualTo(220);
	}

	@Test
	@DisplayName("도면 수정 실패 - 존재하지 않는 도면")
	void updateBlueprintFailNotFound() {
		// given
		Long id = 99L;
		BlueprintRequest request = new BlueprintRequest("x.png", 2, 50.0, 50.0);
		given(blueprintRepository.findById(id)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> blueprintService.updateBlueprint(id, request))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("해당 도면이 존재하지 않습니다.");
	}

	@Test
	@DisplayName("도면 삭제 성공")
	void deleteBlueprintTest() {
		// given
		Long id = 1L;
		given(blueprintRepository.existsById(id)).willReturn(true);

		// when
		blueprintService.deleteBlueprint(id);

		// then
		verify(blueprintRepository).deleteById(id);
	}

	@Test
	@DisplayName("도면 삭제 실패 - 존재하지 않는 도면")
	void deleteBlueprintFailNotFound() {
		// given
		Long id = 999L;
		given(blueprintRepository.existsById(id)).willReturn(false);

		// when & then
		assertThatThrownBy(() -> blueprintService.deleteBlueprint(id))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("해당 도면이 존재하지 않습니다.");
	}

	@Test
	@DisplayName("도면 전체 조회 성공")
	void getAllBlueprintsTest() {
		// given
		given(blueprintRepository.findAll(pageable)).willReturn(blueprintPage);

		// when
		PagedResponse<BlueprintResponse> response = blueprintService.getAllBlueprints(0, 10);

		// then
		assertThat(response.content()).hasSize(2);
		assertThat(response.totalElements()).isEqualTo(2);
		assertThat(response.content().get(0).floor()).isEqualTo(1);
	}
}