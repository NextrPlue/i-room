package com.iroom.admin.service;

import com.iroom.admin.dto.request.SignUpRequest;
import com.iroom.admin.dto.response.SignUpResponse;
import com.iroom.admin.entity.Admin;
import com.iroom.admin.repository.AdminRepository;
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

    public SignUpResponse signUp(SignUpRequest request) {
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

        return new SignUpResponse(
                request.name(),
                request.email(),
                request.phone(),
                request.role()
        );
    }


}
