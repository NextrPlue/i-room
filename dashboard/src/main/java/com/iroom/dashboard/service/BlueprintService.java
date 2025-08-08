package com.iroom.dashboard.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.iroom.dashboard.dto.request.BlueprintRequest;
import com.iroom.dashboard.dto.response.BlueprintResponse;
import com.iroom.dashboard.entity.Blueprint;
import com.iroom.dashboard.repository.BlueprintRepository;
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
	private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
		"image/png", "image/jpeg", "image/jpg");
	private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

	@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN')")
	public BlueprintResponse createBlueprint(MultipartFile file, BlueprintRequest request) {
		String storedFilename = saveFile(file);
		String blueprintUrl = "/uploads/blueprints/" + storedFilename;

		Blueprint blueprint = Blueprint.builder()
			.blueprintUrl(blueprintUrl)
			.floor(request.floor())
			.width(request.width())
			.height(request.height())
			.build();
		return new BlueprintResponse(blueprintRepository.save(blueprint));
	}

	private String saveFile(MultipartFile file) {
		// 파일 검증
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
		
		blueprint.update(blueprintUrl, request.floor(), request.width(), request.height());
		return new BlueprintResponse(blueprint);
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

	@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN')")
	public void deleteBlueprint(Long id) {
		if (!blueprintRepository.existsById(id)) {
			throw new CustomException(ErrorCode.DASHBOARD_BLUEPRINT_NOT_FOUND);
		}
		blueprintRepository.deleteById(id);
	}

	@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_READER')")
	public PagedResponse<BlueprintResponse> getAllBlueprints(int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<Blueprint> blueprints = blueprintRepository.findAll(pageable);
		Page<BlueprintResponse> responsePage = blueprints.map(BlueprintResponse::new);
		return PagedResponse.of(responsePage);
	}

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
}
