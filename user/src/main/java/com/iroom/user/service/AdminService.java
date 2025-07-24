package com.iroom.user.service;

import com.iroom.user.dto.request.*;
import com.iroom.user.dto.response.AdminSignUpResponse;
import com.iroom.user.dto.response.AdminUpdateResponse;
import com.iroom.user.dto.response.LoginResponse;
import com.iroom.user.entity.Admin;
import com.iroom.user.jwt.JwtTokenProvider;
import com.iroom.user.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
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
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        Admin admin = request.toEntity(passwordEncoder);
        adminRepository.save(admin);

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
    }

    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public AdminUpdateResponse updateAdminRole(Long adminId, AdminUpdateRoleRequest request) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("ID " + adminId + "에 해당하는 관리자를 찾을 수 없습니다."));

        admin.updateRole(request.role());

        return new AdminUpdateResponse(admin);
    }
}
