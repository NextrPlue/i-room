package com.iroom.user.service;

import com.iroom.user.dto.request.AdminSignUpRequest;
import com.iroom.user.dto.request.LoginRequest;
import com.iroom.user.dto.response.AdminSignUpResponse;
import com.iroom.user.dto.response.LoginResponse;
import com.iroom.user.entity.Admin;
import com.iroom.user.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminSignUpResponse signUp(AdminSignUpRequest request) {
        if (adminRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        Admin admin = Admin.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .phone(request.phone())
                .role(request.role())
                .build();

        adminRepository.save(admin);

        return new AdminSignUpResponse(
                request.name(),
                request.email(),
                request.phone(),
                request.role()
        );
    }

    public LoginResponse login(LoginRequest request) {
        Admin admin = adminRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        if (!passwordEncoder.matches(request.password(), admin.getPassword())) {
            throw new IllegalArgumentException("잘못된 비밀번호입니다.");
        }

        // JWT 토큰 로직 추가 이후, 이를 반환하도록 수정
        return new LoginResponse("JWT 토큰이 들어가야 함");
    }
}
