package com.iroom.user.admin.service;

import com.iroom.modulecommon.exception.CustomException;
import com.iroom.modulecommon.exception.ErrorCode;
import com.iroom.user.admin.dto.request.AdminSignUpRequest;
import com.iroom.user.admin.dto.request.AdminUpdateInfoRequest;
import com.iroom.user.admin.dto.request.AdminUpdatePasswordRequest;
import com.iroom.user.admin.dto.request.AdminUpdateRoleRequest;
import com.iroom.user.admin.dto.response.AdminInfoResponse;
import com.iroom.user.admin.dto.response.AdminSignUpResponse;
import com.iroom.user.admin.dto.response.AdminUpdateResponse;
import com.iroom.user.common.dto.request.LoginRequest;
import com.iroom.user.common.dto.response.LoginResponse;
import com.iroom.modulecommon.dto.response.PagedResponse;
import com.iroom.user.admin.entity.Admin;
import com.iroom.modulecommon.enums.AdminRole;
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

	public AdminSignUpResponse signUp(AdminSignUpRequest request) {
		if (adminRepository.existsByEmail(request.email())) {
			throw new CustomException(ErrorCode.USER_EMAIL_ALREADY_EXISTS);
		}

		Admin admin = request.toEntity(passwordEncoder);
		adminRepository.save(admin);

		return new AdminSignUpResponse(admin);
	}

	public LoginResponse login(LoginRequest request) {
		Admin admin = adminRepository.findByEmail(request.email())
			.orElseThrow(() -> new CustomException(ErrorCode.USER_UNREGISTERED_EMAIL));

		if (!passwordEncoder.matches(request.password(), admin.getPassword())) {
			throw new CustomException(ErrorCode.USER_INVALID_PASSWORD);
		}

		return new LoginResponse(jwtTokenProvider.createAdminToken(admin));
	}

	@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_READER') and #id == authentication.principal")
	public AdminUpdateResponse updateAdminInfo(Long id, AdminUpdateInfoRequest request) {
		Admin admin = adminRepository.findById(id)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_ADMIN_NOT_FOUND));

		if (!admin.getEmail().equals(request.email()) && adminRepository.existsByEmail(request.email())) {
			throw new CustomException(ErrorCode.USER_EMAIL_ALREADY_EXISTS);
		}

		admin.updateInfo(request.name(), request.email(), request.phone());

		return new AdminUpdateResponse(admin);
	}

	@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_READER') and #id == authentication.principal")
	public void updateAdminPassword(Long id, AdminUpdatePasswordRequest request) {
		Admin admin = adminRepository.findById(id)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_ADMIN_NOT_FOUND));

		if (!passwordEncoder.matches(request.password(), admin.getPassword())) {
			throw new CustomException(ErrorCode.USER_CURRENT_PASSWORD_MISMATCH);
		}

		admin.updatePassword(passwordEncoder.encode(request.newPassword()));
	}

	@PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') and #adminId != authentication.principal")
	public AdminUpdateResponse updateAdminRole(Long adminId, AdminUpdateRoleRequest request) {
		Admin admin = adminRepository.findById(adminId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_ADMIN_NOT_FOUND));

		admin.updateRole(request.role());

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
			.orElseThrow(() -> new CustomException(ErrorCode.USER_ADMIN_NOT_FOUND));

		return new AdminInfoResponse(admin);
	}

	@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN')")
	public AdminInfoResponse getAdminById(Long adminId) {
		Admin admin = adminRepository.findById(adminId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_ADMIN_NOT_FOUND));

		return new AdminInfoResponse(admin);
	}

	@PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') and #adminId != authentication.principal")
	public void deleteAdmin(Long adminId) {
		Admin admin = adminRepository.findById(adminId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_ADMIN_NOT_FOUND));

		adminRepository.delete(admin);
	}
}
