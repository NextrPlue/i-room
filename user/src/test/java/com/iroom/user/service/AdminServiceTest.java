package com.iroom.user.service;

import com.iroom.user.dto.request.AdminSignUpRequest;
import com.iroom.user.dto.response.AdminSignUpResponse;
import com.iroom.user.entity.Admin;
import com.iroom.user.enums.AdminRole;
import com.iroom.user.jwt.JwtTokenProvider;
import com.iroom.user.repository.AdminRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class AdminServiceTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AdminService adminService;

    private Admin admin;

    @BeforeEach
    void setUp() {
        admin = Admin.builder()
                .name("admin")
                .email("admin@example.com")
                .password("encodedPassword")
                .role(AdminRole.ADMIN)
                .build();
    }

    @Test
    @DisplayName("관리자 회원 가입 성공")
    void signUpTest() {
        // given
        AdminSignUpRequest request = new AdminSignUpRequest("admin", "admin@example.com", "password", "010-1234-5678");

        given(adminRepository.existsByEmail(request.email())).willReturn(false);
        given(adminRepository.save(any(Admin.class))).willReturn(admin);

        // when
        AdminSignUpResponse response = adminService.signUp(request);

        // then
        assertThat(response.name()).isEqualTo(admin.getName());
        assertThat(response.email()).isEqualTo(admin.getEmail());
    }

    @Test
    @DisplayName("관리자 회원가입 실패 - 이메일 중복")
    void signUpFailEmailExists() {
        // given
        AdminSignUpRequest request = new AdminSignUpRequest("admin", "admin@example.com", "password", "010-1234-5678");

        given(adminRepository.existsByEmail(request.email())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> adminService.signUp(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 사용 중인 이메일입니다.");
    }
}
