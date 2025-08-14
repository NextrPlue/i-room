package com.iroom.dashboard.blueprint.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.iroom.dashboard.blueprint.dto.request.BlueprintRequest;
import com.iroom.dashboard.blueprint.dto.request.BlueprintRequest.GeoPointDto;
import com.iroom.dashboard.blueprint.dto.response.BlueprintResponse;
import com.iroom.dashboard.blueprint.entity.Blueprint;
import com.iroom.dashboard.blueprint.entity.GeoPoint;
import com.iroom.dashboard.blueprint.repository.BlueprintRepository;
import com.iroom.modulecommon.dto.response.PagedResponse;
import com.iroom.modulecommon.exception.CustomException;
import com.iroom.modulecommon.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
public class BlueprintService {

	private final BlueprintRepository blueprintRepository;

	@Value("${app.upload-dir}")
	private String uploadDir;

	private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".png", ".jpg", ".jpeg");
	private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList("image/png", "image/jpeg", "image/jpg");
	private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

	// ====== CREATE ======
	@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN')")
	public BlueprintResponse createBlueprint(MultipartFile file, BlueprintRequest request) {
		String storedFilename = saveFile(file);
		String blueprintUrl = "/uploads/blueprints/" + storedFilename;

		// 코너 4개 유효성 체크
		validateCorners(request);

		Blueprint blueprint = Blueprint.builder()
			.name(request.name())
			.blueprintUrl(blueprintUrl)
			.floor(request.floor())
			.width(request.width())
			.height(request.height())
			.topLeft(toGeo(request.topLeft()))
			.topRight(toGeo(request.topRight()))
			.bottomRight(toGeo(request.bottomRight()))
			.bottomLeft(toGeo(request.bottomLeft()))
			.build();

		return new BlueprintResponse(blueprintRepository.save(blueprint));
	}

	// ====== UPDATE ======
	@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN')")
	public BlueprintResponse updateBlueprint(Long id, BlueprintRequest request, MultipartFile file) {
		Blueprint blueprint = blueprintRepository.findById(id)
			.orElseThrow(() -> new CustomException(ErrorCode.DASHBOARD_BLUEPRINT_NOT_FOUND));

		String blueprintUrl = blueprint.getBlueprintUrl();

		if (file != null && !file.isEmpty()) {
			deleteExistingFile(blueprint.getBlueprintUrl());
			String storedFilename = saveFile(file);
			blueprintUrl = "/uploads/blueprints/" + storedFilename;
		}

		// 코너 4개 유효성 체크
		validateCorners(request);

		blueprint.update(
			request.name(),
			blueprintUrl,
			request.floor(),
			request.width(),
			request.height(),
			toGeo(request.topLeft()),
			toGeo(request.topRight()),
			toGeo(request.bottomRight()),
			toGeo(request.bottomLeft())
		);

		return new BlueprintResponse(blueprint);
	}

	// ====== DELETE ======
	@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN')")
	public void deleteBlueprint(Long id) {
		Blueprint blueprint = blueprintRepository.findById(id)
			.orElseThrow(() -> new CustomException(ErrorCode.DASHBOARD_BLUEPRINT_NOT_FOUND));
		deleteExistingFile(blueprint.getBlueprintUrl());
		blueprintRepository.deleteById(id);
	}

	// ====== READ (single) ======
	@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_READER')")
	public BlueprintResponse getBlueprint(Long id) {
		Blueprint blueprint = blueprintRepository.findById(id)
			.orElseThrow(() -> new CustomException(ErrorCode.DASHBOARD_BLUEPRINT_NOT_FOUND));
		return new BlueprintResponse(blueprint);
	}

	// ====== READ (paged list) ======
	@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_READER')")
	public PagedResponse<BlueprintResponse> getAllBlueprints(String target, String keyword, Integer page, Integer size) {
		Pageable pageable = PageRequest.of(page, size);

		Page<Blueprint> blueprintPage;
		if (target == null || keyword == null || keyword.trim().isEmpty()) {
			blueprintPage = blueprintRepository.findAll(pageable);
		} else if ("floor".equals(target)) {
			try {
				Integer floor = Integer.parseInt(keyword);
				blueprintPage = blueprintRepository.findByFloor(floor, pageable);
			} catch (NumberFormatException e) {
				blueprintPage = blueprintRepository.findAll(pageable);
			}
		} else if ("name".equals(target)) {
			blueprintPage = blueprintRepository.findByName(keyword.trim(), pageable);
		} else {
			blueprintPage = blueprintRepository.findAll(pageable);
		}

		Page<BlueprintResponse> responsePage = blueprintPage.map(BlueprintResponse::new);
		return PagedResponse.of(responsePage);
	}

	// ====== IMAGE RESOURCE ======
	@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_READER')")
	public Resource getBlueprintImageResource(Long id) {
		Blueprint blueprint = blueprintRepository.findById(id)
			.orElseThrow(() -> new CustomException(ErrorCode.DASHBOARD_BLUEPRINT_NOT_FOUND));

		if (blueprint.getBlueprintUrl() == null || blueprint.getBlueprintUrl().isEmpty()) {
			throw new CustomException(ErrorCode.DASHBOARD_BLUEPRINT_NOT_FOUND);
		}

		try {
			// 파일 경로에서 파일명 추출 (/uploads/blueprints/filename.jpg -> filename.jpg)
			String filename = blueprint.getBlueprintUrl().substring(blueprint.getBlueprintUrl().lastIndexOf("/") + 1);

			String rootPath = System.getProperty("user.dir");
			String filePath = Paths.get(rootPath, uploadDir, filename).toString();

			File file = new File(filePath);
			if (!file.exists()) {
				throw new CustomException(ErrorCode.DASHBOARD_BLUEPRINT_NOT_FOUND);
			}

			return new FileSystemResource(file);

		} catch (Exception e) {
			throw new CustomException(ErrorCode.DASHBOARD_BLUEPRINT_NOT_FOUND);
		}
	}

	// ====== PRIVATE HELPERS ======
	private String saveFile(MultipartFile file) {
		validateFileBasic(file);

		File directory = createUploadDirectory();
		String storedFilename = generateFilename(file);

		File destFile = new File(directory, storedFilename);
		try {
			file.transferTo(destFile);
		} catch (IOException e) {
			throw new RuntimeException("도면 파일 저장 중 오류 발생", e);
		}
		return storedFilename;
	}

	private void validateFileBasic(MultipartFile file) {
		// 파일 크기 검증
		if (file.getSize() > MAX_FILE_SIZE) {
			throw new CustomException(ErrorCode.DASHBOARD_FILE_TOO_LARGE);
		}

		// MIME 타입 검증
		String contentType = file.getContentType();
		if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType)) {
			throw new CustomException(ErrorCode.DASHBOARD_INVALID_FILE_TYPE);
		}
	}

	private String generateFilename(MultipartFile file) {
		String originalFilename = file.getOriginalFilename();
		if (originalFilename == null || originalFilename.trim().isEmpty()) {
			throw new CustomException(ErrorCode.DASHBOARD_INVALID_FILE_NAME);
		}

		String cleanedFilename = StringUtils.cleanPath(originalFilename);
		if (!cleanedFilename.contains(".")) {
			throw new CustomException(ErrorCode.DASHBOARD_INVALID_FILE_NAME);
		}

		String fileExtension = cleanedFilename.substring(cleanedFilename.lastIndexOf(".")).toLowerCase();
		if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
			throw new CustomException(ErrorCode.DASHBOARD_INVALID_FILE_TYPE);
		}

		return UUID.randomUUID() + fileExtension;
	}

	private File createUploadDirectory() {
		String rootPath = System.getProperty("user.dir");
		String saveDirPath = Paths.get(rootPath, uploadDir).toString();

		File directory = new File(saveDirPath);
		if (!directory.exists()) {
			if (!directory.mkdirs()) {
				throw new RuntimeException("업로드 디렉토리 생성에 실패했습니다: " + saveDirPath);
			}
		}
		return directory;
	}

	private void deleteExistingFile(String blueprintUrl) {
		if (blueprintUrl != null && !blueprintUrl.isEmpty()) {
			try {
				String filename = blueprintUrl.substring(blueprintUrl.lastIndexOf("/") + 1);
				String rootPath = System.getProperty("user.dir");
				String filePath = Paths.get(rootPath, uploadDir, filename).toString();
				File file = new File(filePath);
				if (file.exists()) {
					file.delete();
				}
			} catch (Exception e) {
				System.err.println("기존 파일 삭제 실패: " + e.getMessage());
			}
		}
	}

	// ---- 좌표 매핑 유틸 ----
	private GeoPoint toGeo(GeoPointDto dto) {
		if (dto == null || dto.lat() == null || dto.lon() == null) {
			throw new CustomException(ErrorCode.GLOBAL_VALIDATION_FAILED);
		}
		return new GeoPoint(dto.lat(), dto.lon());
	}

	private void validateCorners(BlueprintRequest r) {
		if (r.topLeft() == null || r.topRight() == null ||
			r.bottomRight() == null || r.bottomLeft() == null) {
			throw new CustomException(ErrorCode.GLOBAL_VALIDATION_FAILED);
		}
	}
}
