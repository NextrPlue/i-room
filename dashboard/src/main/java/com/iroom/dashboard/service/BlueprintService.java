package com.iroom.dashboard.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;

import com.iroom.dashboard.dto.request.BlueprintRequest;
import com.iroom.dashboard.dto.response.BlueprintResponse;
import com.iroom.dashboard.entity.Blueprint;
import com.iroom.dashboard.repository.BlueprintRepository;
import com.iroom.modulecommon.dto.response.PagedResponse;

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

	@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN')")
	public BlueprintResponse createBlueprint(MultipartFile file,BlueprintRequest request) {
		String rootPath = System.getProperty("user.dir");
		String saveDirPath = Paths.get(rootPath, uploadDir).toString();

		File directory = new File(saveDirPath);
		if (!directory.exists()) {
			directory.mkdirs();
		}

		String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
		String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
		String storedFilename = UUID.randomUUID() + fileExtension;

		File destFile = new File(directory, storedFilename);
		try {
			file.transferTo(destFile);
		} catch (IOException e) {
			throw new RuntimeException("도면 파일 저장 중 오류 발생", e);
		}

		// 저장된 정적 파일의 접근 경로(URL)
		String blueprintUrl = "/uploads/blueprints/" + storedFilename;

		Blueprint blueprint = Blueprint.builder()
			.blueprintUrl(blueprintUrl)
			.floor(request.floor())
			.width(request.width())
			.height(request.height())
			.build();
		return new BlueprintResponse(blueprintRepository.save(blueprint));
	}

	@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN')")
	public BlueprintResponse updateBlueprint(Long id, BlueprintRequest request) {
		Blueprint blueprint = blueprintRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("해당 도면이 존재하지 않습니다."));
		blueprint.update(request.blueprintUrl(), request.floor(), request.width(), request.height());
		return new BlueprintResponse(blueprint);
	}

	@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN')")
	public void deleteBlueprint(Long id) {
		if (!blueprintRepository.existsById(id)) {
			throw new IllegalArgumentException("해당 도면이 존재하지 않습니다.");
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
}
