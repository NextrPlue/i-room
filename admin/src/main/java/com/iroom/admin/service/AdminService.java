package com.iroom.admin.service;

import com.iroom.admin.dto.request.SignUpRequest;
import com.iroom.admin.dto.response.SignUpResponse;
import com.iroom.admin.entity.Admin;
import com.iroom.admin.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;

    public SignUpResponse signUp(SignUpRequest request) {
        // 회원가입 로직 구현 필요

        Admin admin = Admin.builder()
                .name(request.name())
                .email(request.email())
                .password(request.password())
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
