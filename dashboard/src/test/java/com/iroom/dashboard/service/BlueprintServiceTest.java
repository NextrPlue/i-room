package com.iroom.dashboard.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
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
import com.iroom.modulecommon.exception.CustomException;
import com.iroom.modulecommon.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
class BlueprintServiceTest {

	@TempDir
	Path tempDir;

	@Mock
	private BlueprintRepository blueprintRepository;

	@InjectMocks
	private BlueprintService blueprintService;

	private Blueprint blueprint;
	private Pageable pageable;
	private Page<Blueprint> blueprintPage;
	private MultipartFile validFile;
	private MultipartFile invalidMimeTypeFile;
	private MultipartFile invalidExtensionFile;
	private MultipartFile nullNameFile;
	private MultipartFile largeSizeFile;
	private String originalUserDir;

	@BeforeEach
	void setUp() {
		// 원래 user.dir 백업
		originalUserDir = System.getProperty("user.dir");

		// 임시 디렉토리를 user.dir로 설정하여 파일이 tempDir에 생성되도록 함
		System.setProperty("user.dir", tempDir.toString());

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
		
		invalidMimeTypeFile = new MockMultipartFile(
			"file",
			"test-blueprint.png",
			"text/plain", // 잘못된 MIME 타입
			"test content".getBytes()
		);
		
		invalidExtensionFile = new MockMultipartFile(
			"file",
			"test-blueprint.txt", // 잘못된 확장자
			"image/png",
			"test content".getBytes()
		);
		
		nullNameFile = new MockMultipartFile(
			"file",
			null, // null 파일명
			"image/png",
			"test content".getBytes()
		);
		
		// 큰 파일 생성 (10MB 초과)
		byte[] largeContent = new byte[11 * 1024 * 1024]; // 11MB
		largeSizeFile = new MockMultipartFile(
			"file",
			"large-blueprint.png",
			"image/png",
			largeContent
		);
		
		// upload-dir 설정
		ReflectionTestUtils.setField(blueprintService, "uploadDir", "uploads/blueprints");
	}

	@AfterEach
	void tearDown() {
		// 원래 user.dir 복원
		System.setProperty("user.dir", originalUserDir);
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
			.isInstanceOf(CustomException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.DASHBOARD_INVALID_FILE_NAME);
	}
	
	@Test
	@DisplayName("도면 생성 실패 - 잘못된 MIME 타입")
	void createBlueprintFailInvalidMimeType() {
		// given
		BlueprintRequest request = new BlueprintRequest(null, 1, 100.0, 200.0);

		// when & then
		assertThatThrownBy(() -> blueprintService.createBlueprint(invalidMimeTypeFile, request))
			.isInstanceOf(CustomException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.DASHBOARD_INVALID_FILE_TYPE);
	}
	
	@Test
	@DisplayName("도면 생성 실패 - 잘못된 파일 확장자")
	void createBlueprintFailInvalidExtension() {
		// given
		BlueprintRequest request = new BlueprintRequest(null, 1, 100.0, 200.0);

		// when & then
		assertThatThrownBy(() -> blueprintService.createBlueprint(invalidExtensionFile, request))
			.isInstanceOf(CustomException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.DASHBOARD_INVALID_FILE_TYPE);
	}
	
	@Test
	@DisplayName("도면 생성 실패 - 파일 크기 초과")
	void createBlueprintFailFileTooLarge() {
		// given
		BlueprintRequest request = new BlueprintRequest(null, 1, 100.0, 200.0);

		// when & then
		assertThatThrownBy(() -> blueprintService.createBlueprint(largeSizeFile, request))
			.isInstanceOf(CustomException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.DASHBOARD_FILE_TOO_LARGE);
	}

	@Test
	@DisplayName("도면 수정 성공 - 파일 없이 정보만 수정")
	void updateBlueprintWithoutFileTest() {
		// given
		Long id = 1L;
		BlueprintRequest request = new BlueprintRequest(null, 1, 120.0, 220.0);
		given(blueprintRepository.findById(id)).willReturn(Optional.of(blueprint));

		// when
		BlueprintResponse response = blueprintService.updateBlueprint(id, request, null);

		// then
		assertThat(response.blueprintUrl()).isEqualTo("/uploads/blueprints/test-uuid.png"); // 기존 URL 유지
		assertThat(response.width()).isEqualTo(120.0);
		assertThat(response.height()).isEqualTo(220.0);
	}

	@Test
	@DisplayName("도면 수정 성공 - 새 파일과 함께 수정")
	void updateBlueprintWithFileTest() {
		// given
		Long id = 1L;
		BlueprintRequest request = new BlueprintRequest(null, 1, 120.0, 220.0);
		given(blueprintRepository.findById(id)).willReturn(Optional.of(blueprint));

		// when
		BlueprintResponse response = blueprintService.updateBlueprint(id, request, validFile);

		// then
		assertThat(response.blueprintUrl()).contains("/uploads/blueprints/");
		assertThat(response.blueprintUrl()).contains(".png");
		assertThat(response.blueprintUrl()).isNotEqualTo("/uploads/blueprints/test-uuid.png"); // 새 URL로 변경
		assertThat(response.width()).isEqualTo(120.0);
		assertThat(response.height()).isEqualTo(220.0);
	}

	@Test
	@DisplayName("도면 수정 실패 - 존재하지 않는 도면")
	void updateBlueprintFailNotFound() {
		// given
		Long id = 99L;
		BlueprintRequest request = new BlueprintRequest(null, 2, 50.0, 50.0);
		given(blueprintRepository.findById(id)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> blueprintService.updateBlueprint(id, request, null))
			.isInstanceOf(CustomException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.DASHBOARD_BLUEPRINT_NOT_FOUND);
	}

	@Test
	@DisplayName("도면 수정 실패 - 잘못된 파일")
	void updateBlueprintFailInvalidFile() {
		// given
		Long id = 1L;
		BlueprintRequest request = new BlueprintRequest(null, 1, 120.0, 220.0);
		given(blueprintRepository.findById(id)).willReturn(Optional.of(blueprint));

		// when & then
		assertThatThrownBy(() -> blueprintService.updateBlueprint(id, request, invalidMimeTypeFile))
			.isInstanceOf(CustomException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.DASHBOARD_INVALID_FILE_TYPE);
	}

	@Test
	@DisplayName("도면 삭제 성공")
	void deleteBlueprintTest() {
		// given
		Long id = 1L;
		given(blueprintRepository.findById(id)).willReturn(Optional.of(blueprint));

		// when
		blueprintService.deleteBlueprint(id);

		// then
		verify(blueprintRepository).deleteById(id);
		verify(blueprintRepository).findById(id);
	}

	@Test
	@DisplayName("도면 삭제 실패 - 존재하지 않는 도면")
	void deleteBlueprintFailNotFound() {
		// given
		Long id = 999L;
		given(blueprintRepository.findById(id)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> blueprintService.deleteBlueprint(id))
			.isInstanceOf(CustomException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.DASHBOARD_BLUEPRINT_NOT_FOUND);
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
	
	@Test
	@DisplayName("도면 이미지 리소스 조회 성공")
	void getBlueprintImageResourceTest() throws IOException {
		// given
		Long id = 1L;
		
		// tempDir 내에 uploads/blueprints 디렉토리와 파일 생성
		File uploadsDir = tempDir.resolve("uploads").resolve("blueprints").toFile();
		uploadsDir.mkdirs();
		File testFile = new File(uploadsDir, "test-image.png");
		testFile.createNewFile();
		
		Blueprint blueprintWithImage = Blueprint.builder()
			.blueprintUrl("/uploads/blueprints/test-image.png")
			.floor(1)
			.width(100.0)
			.height(200.0)
			.build();
			
		given(blueprintRepository.findById(id)).willReturn(Optional.of(blueprintWithImage));
		
		// when
		Resource resource = blueprintService.getBlueprintImageResource(id);
		
		// then
		assertThat(resource).isNotNull();
		assertThat(resource.exists()).isTrue();
	}
	
	@Test
	@DisplayName("도면 이미지 리소스 조회 실패 - 존재하지 않는 도면")
	void getBlueprintImageResourceFailNotFound() {
		// given
		Long id = 999L;
		given(blueprintRepository.findById(id)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> blueprintService.getBlueprintImageResource(id))
			.isInstanceOf(CustomException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.DASHBOARD_BLUEPRINT_NOT_FOUND);
	}
	
	@Test
	@DisplayName("도면 이미지 리소스 조회 실패 - blueprintUrl이 null")
	void getBlueprintImageResourceFailNullUrl() {
		// given
		Long id = 1L;
		Blueprint blueprintWithNullUrl = Blueprint.builder()
			.blueprintUrl(null)
			.floor(1)
			.width(100.0)
			.height(200.0)
			.build();
			
		given(blueprintRepository.findById(id)).willReturn(Optional.of(blueprintWithNullUrl));

		// when & then
		assertThatThrownBy(() -> blueprintService.getBlueprintImageResource(id))
			.isInstanceOf(CustomException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.DASHBOARD_BLUEPRINT_NOT_FOUND);
	}
}