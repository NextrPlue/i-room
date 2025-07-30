package com.iroom.user.admin.service;

import com.iroom.user.admin.dto.request.AdminSignUpRequest;
import com.iroom.user.admin.dto.request.AdminUpdateInfoRequest;
import com.iroom.user.admin.dto.request.AdminUpdatePasswordRequest;
import com.iroom.user.admin.dto.request.AdminUpdateRoleRequest;
import com.iroom.user.admin.dto.response.AdminInfoResponse;
import com.iroom.user.admin.dto.response.AdminSignUpResponse;
import com.iroom.user.admin.dto.response.AdminUpdateResponse;
import com.iroom.user.common.dto.request.LoginRequest;
import com.iroom.user.common.dto.response.LoginResponse;
import com.iroom.user.common.dto.response.PagedResponse;
import com.iroom.user.common.service.KafkaProducerService;
import com.iroom.user.admin.dto.event.AdminEvent;
import com.iroom.user.admin.entity.Admin;
import com.iroom.user.admin.enums.AdminRole;
import com.iroom.user.common.jwt.JwtTokenProvider;
import com.iroom.user.admin.repository.AdminRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminService {

	private final AdminRepository adminRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final KafkaProducerService kafkaProducerService;

	public AdminSignUpResponse signUp(AdminSignUpRequest request) {
		if (adminRepository.existsByEmail(request.email())) {
			throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
		}

		Admin admin = request.toEntity(passwordEncoder);
		adminRepository.save(admin);

		kafkaProducerService.publishMessage("ADMIN_CREATED", new AdminEvent(admin));

		return new AdminSignUpResponse(admin);
	}

	public LoginResponse login(LoginRequest request) {
		Admin admin = adminRepository.findByEmail(request.email())
			.orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

		if (!passwordEncoder.matches(request.password(), admin.getPassword())) {
			throw new IllegalArgumentException("잘못된 비밀번호입니다.");
		}

		return new LoginResponse(jwtTokenProvider.createAdminToken(admin));
	}

	@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_READER') and #id == authentication.principal")
	public AdminUpdateResponse updateAdminInfo(Long id, AdminUpdateInfoRequest request) {
		Admin admin = adminRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("해당하는 관리자를 찾을 수 없습니다."));

		if (!admin.getEmail().equals(request.email()) && adminRepository.existsByEmail(request.email())) {
			throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
		}

		admin.updateInfo(request.name(), request.email(), request.phone());

		kafkaProducerService.publishMessage("ADMIN_UPDATED", new AdminEvent(admin));

		return new AdminUpdateResponse(admin);
	}

	@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_READER') and #id == authentication.principal")
	public void updateAdminPassword(Long id, AdminUpdatePasswordRequest request) {
		Admin admin = adminRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("해당하는 관리자를 찾을 수 없습니다."));

		if (!passwordEncoder.matches(request.password(), admin.getPassword())) {
			throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
		}

		admin.updatePassword(passwordEncoder.encode(request.newPassword()));

		kafkaProducerService.publishMessage("ADMIN_UPDATED", new AdminEvent(admin));
	}

	@PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') and #adminId != authentication.principal")
	public AdminUpdateResponse updateAdminRole(Long adminId, AdminUpdateRoleRequest request) {
		Admin admin = adminRepository.findById(adminId)
			.orElseThrow(() -> new IllegalArgumentException("ID " + adminId + "에 해당하는 관리자를 찾을 수 없습니다."));

		admin.updateRole(request.role());

		kafkaProducerService.publishMessage("ADMIN_UPDATED", new AdminEvent(admin));

		return new AdminUpdateResponse(admin);
	}

	@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_READER')")
	public PagedResponse<AdminInfoResponse> getAdmins(String target, String keyword, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);

		Page<Admin> adminPage;
		if (target == null || keyword == null || keyword.trim().isEmpty()) {
			adminPage = adminRepository.findAll(pageable);
		} else if ("name".equals(target)) {
			adminPage = adminRepository.findByNameContaining(keyword, pageable);
		} else if ("email".equals(target)) {
			adminPage = adminRepository.findByEmailContaining(keyword, pageable);
		} else if ("role".equals(target)) {
			try {
				AdminRole role = AdminRole.valueOf(keyword.toUpperCase());
				adminPage = adminRepository.findByRole(role, pageable);
			} catch (IllegalArgumentException e) {
				adminPage = adminRepository.findAll(pageable);
			}
		} else {
			adminPage = adminRepository.findAll(pageable);
		}

		Page<AdminInfoResponse> responsePage = adminPage.map(AdminInfoResponse::new);

		return PagedResponse.of(responsePage);
	}

	@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_READER') and #id == authentication.principal")
	public AdminInfoResponse getAdminInfo(Long id) {
		Admin admin = adminRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("해당하는 관리자를 찾을 수 없습니다."));

		return new AdminInfoResponse(admin);
	}

	@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN')")
	public AdminInfoResponse getAdminById(Long adminId) {
		Admin admin = adminRepository.findById(adminId)
			.orElseThrow(() -> new IllegalArgumentException("해당하는 관리자를 찾을 수 없습니다."));

		return new AdminInfoResponse(admin);
	}

	@PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') and #adminId != authentication.principal")
	public void deleteAdmin(Long adminId) {
		Admin admin = adminRepository.findById(adminId)
			.orElseThrow(() -> new IllegalArgumentException("ID " + adminId + "에 해당하는 관리자를 찾을 수 없습니다."));

		adminRepository.delete(admin);

		kafkaProducerService.publishMessage("ADMIN_DELETED", new AdminEvent(admin));
	}
}
