package com.iroom.user.service;

import com.iroom.user.admin.dto.request.AdminSignUpRequest;
import com.iroom.user.admin.dto.request.AdminUpdateInfoRequest;
import com.iroom.user.admin.dto.request.AdminUpdatePasswordRequest;
import com.iroom.user.admin.dto.request.AdminUpdateRoleRequest;
import com.iroom.user.admin.dto.response.AdminInfoResponse;
import com.iroom.user.admin.dto.response.AdminSignUpResponse;
import com.iroom.user.admin.dto.response.AdminUpdateResponse;
import com.iroom.user.admin.service.AdminService;
import com.iroom.user.common.dto.request.LoginRequest;
import com.iroom.user.common.dto.response.LoginResponse;
import com.iroom.user.common.dto.response.PagedResponse;
import com.iroom.user.common.service.KafkaProducerService;
import com.iroom.user.admin.entity.Admin;
import com.iroom.user.admin.enums.AdminRole;
import com.iroom.user.common.jwt.JwtTokenProvider;
import com.iroom.user.admin.repository.AdminRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AdminServiceTest {

	@Mock
	private AdminRepository adminRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtTokenProvider jwtTokenProvider;

	@Mock
	private KafkaProducerService kafkaProducerService;

	@InjectMocks
	private AdminService adminService;

	private Admin admin;
	private Pageable pageable;
	private Page<Admin> adminPage;

	@BeforeEach
	void setUp() {
		admin = Admin.builder()
			.name("admin")
			.email("admin@example.com")
			.password("encodedPassword")
			.role(AdminRole.ADMIN)
			.build();

		Admin admin2 = Admin.builder()
			.name("admin2")
			.email("admin2@example.com")
			.password("encodedPassword")
			.role(AdminRole.READER)
			.build();

		pageable = PageRequest.of(0, 10);
		adminPage = new PageImpl<>(List.of(admin, admin2), pageable, 2);
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
		verify(kafkaProducerService).publishMessage(eq("ADMIN_CREATED"), any());
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

	@Test
	@DisplayName("관리자 로그인 성공")
	void loginTest() {
		// given
		LoginRequest request = new LoginRequest("admin@example.com", "password");
		String token = "jwt-token";

		given(adminRepository.findByEmail(request.email())).willReturn(Optional.of(admin));
		given(passwordEncoder.matches(request.password(), admin.getPassword())).willReturn(true);
		given(jwtTokenProvider.createAdminToken(admin)).willReturn(token);

		// when
		LoginResponse response = adminService.login(request);

		// then
		assertThat(response.token()).isEqualTo(token);
	}

	@Test
	@DisplayName("관리자 로그인 실패 - 존재하지 않는 이메일")
	void loginFailEmailNotExists() {
		// given
		LoginRequest request = new LoginRequest("nonexistent@example.com", "password");

		given(adminRepository.findByEmail(request.email())).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> adminService.login(request))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("가입되지 않은 이메일입니다.");
	}

	@Test
	@DisplayName("관리자 로그인 실패 - 잘못된 비밀번호")
	void loginFailWrongPassword() {
		// given
		LoginRequest request = new LoginRequest("admin@example.com", "wrongpassword");

		given(adminRepository.findByEmail(request.email())).willReturn(Optional.of(admin));
		given(passwordEncoder.matches(request.password(), admin.getPassword())).willReturn(false);

		// when & then
		assertThatThrownBy(() -> adminService.login(request))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("잘못된 비밀번호입니다.");
	}

	@Test
	@DisplayName("관리자 정보 수정 성공")
	void updateAdminInfoTest() {
		// given
		Long adminId = 1L;
		AdminUpdateInfoRequest request = new AdminUpdateInfoRequest("updatedName", "updated@example.com",
			"010-9876-5432");

		given(adminRepository.findById(adminId)).willReturn(Optional.of(admin));
		given(adminRepository.existsByEmail(request.email())).willReturn(false);

		// when
		AdminUpdateResponse response = adminService.updateAdminInfo(adminId, request);

		// then
		assertThat(response.name()).isEqualTo(request.name());
		assertThat(response.email()).isEqualTo(request.email());
		verify(kafkaProducerService).publishMessage(eq("ADMIN_UPDATED"), any());
	}

	@Test
	@DisplayName("관리자 정보 수정 실패 - 존재하지 않는 관리자")
	void updateAdminInfoFailAdminNotFound() {
		// given
		Long adminId = 999L;
		AdminUpdateInfoRequest request = new AdminUpdateInfoRequest("updatedName", "updated@example.com",
			"010-9876-5432");

		given(adminRepository.findById(adminId)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> adminService.updateAdminInfo(adminId, request))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("해당하는 관리자를 찾을 수 없습니다.");
	}

	@Test
	@DisplayName("관리자 정보 수정 실패 - 이메일 중복")
	void updateAdminInfoFailEmailExists() {
		// given
		Long adminId = 1L;
		AdminUpdateInfoRequest request = new AdminUpdateInfoRequest("updatedName", "updated@example.com",
			"010-9876-5432");

		given(adminRepository.findById(adminId)).willReturn(Optional.of(admin));
		given(adminRepository.existsByEmail(request.email())).willReturn(true);

		// when & then
		assertThatThrownBy(() -> adminService.updateAdminInfo(adminId, request))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("이미 사용 중인 이메일입니다.");
	}

	@Test
	@DisplayName("관리자 비밀번호 변경 성공")
	void updateAdminPasswordTest() {
		// given
		Long adminId = 1L;
		AdminUpdatePasswordRequest request = new AdminUpdatePasswordRequest("currentPassword", "newPassword");

		given(adminRepository.findById(adminId)).willReturn(Optional.of(admin));
		given(passwordEncoder.matches(request.password(), admin.getPassword())).willReturn(true);
		given(passwordEncoder.encode(request.newPassword())).willReturn("encodedNewPassword");

		// when
		adminService.updateAdminPassword(adminId, request);

		// then
		verify(passwordEncoder).encode(request.newPassword());
		verify(kafkaProducerService).publishMessage(eq("ADMIN_UPDATED"), any());
	}

	@Test
	@DisplayName("관리자 비밀번호 변경 실패 - 존재하지 않는 관리자")
	void updateAdminPasswordFailAdminNotFound() {
		// given
		Long adminId = 999L;
		AdminUpdatePasswordRequest request = new AdminUpdatePasswordRequest("currentPassword", "newPassword");

		given(adminRepository.findById(adminId)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> adminService.updateAdminPassword(adminId, request))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("해당하는 관리자를 찾을 수 없습니다.");
	}

	@Test
	@DisplayName("관리자 비밀번호 변경 실패 - 현재 비밀번호 불일치")
	void updateAdminPasswordFailWrongPassword() {
		// given
		Long adminId = 1L;
		AdminUpdatePasswordRequest request = new AdminUpdatePasswordRequest("wrongPassword", "newPassword");

		given(adminRepository.findById(adminId)).willReturn(Optional.of(admin));
		given(passwordEncoder.matches(request.password(), admin.getPassword())).willReturn(false);

		// when & then
		assertThatThrownBy(() -> adminService.updateAdminPassword(adminId, request))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("현재 비밀번호가 일치하지 않습니다.");
	}

	@Test
	@DisplayName("관리자 권한 수정 성공")
	void updateAdminRoleTest() {
		// given
		Long adminId = 1L;
		AdminUpdateRoleRequest request = new AdminUpdateRoleRequest(AdminRole.READER);

		given(adminRepository.findById(adminId)).willReturn(Optional.of(admin));

		// when
		AdminUpdateResponse response = adminService.updateAdminRole(adminId, request);

		// then
		assertThat(response.role()).isEqualTo(AdminRole.READER);
		verify(kafkaProducerService).publishMessage(eq("ADMIN_UPDATED"), any());
	}

	@Test
	@DisplayName("관리자 권한 수정 실패 - 존재하지 않는 관리자")
	void updateAdminRoleFailAdminNotFound() {
		// given
		Long adminId = 999L;
		AdminUpdateRoleRequest request = new AdminUpdateRoleRequest(AdminRole.READER);

		given(adminRepository.findById(adminId)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> adminService.updateAdminRole(adminId, request))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("ID " + adminId + "에 해당하는 관리자를 찾을 수 없습니다.");
	}

	@Test
	@DisplayName("관리자 목록 조회 - 전체 조회")
	void getAdminsTest() {
		// given
		given(adminRepository.findAll(pageable)).willReturn(adminPage);

		// when
		PagedResponse<AdminInfoResponse> response = adminService.getAdmins(null, null, 0, 10);

		// then
		assertThat(response.content()).hasSize(2);
		assertThat(response.totalElements()).isEqualTo(2);
	}

	@Test
	@DisplayName("관리자 목록 조회 - 이름으로 검색")
	void getAdminsSearchByName() {
		// given
		given(adminRepository.findByNameContaining("admin", pageable)).willReturn(adminPage);

		// when
		PagedResponse<AdminInfoResponse> response = adminService.getAdmins("name", "admin", 0, 10);

		// then
		assertThat(response.content()).hasSize(2);
		assertThat(response.content().get(0).name()).isEqualTo(admin.getName());
	}

	@Test
	@DisplayName("관리자 목록 조회 - 이메일로 검색")
	void getAdminsSearchByEmail() {
		// given
		given(adminRepository.findByEmailContaining("admin", pageable)).willReturn(adminPage);

		// when
		PagedResponse<AdminInfoResponse> response = adminService.getAdmins("email", "admin", 0, 10);

		// then
		assertThat(response.content()).hasSize(2);
		assertThat(response.content().get(0).email()).isEqualTo(admin.getEmail());
	}

	@Test
	@DisplayName("관리자 목록 조회 - 역할로 검색")
	void getAdminsSearchByRole() {
		// given
		given(adminRepository.findByRole(AdminRole.ADMIN, pageable)).willReturn(
			new PageImpl<>(List.of(admin), pageable, 1));

		// when
		PagedResponse<AdminInfoResponse> response = adminService.getAdmins("role", "ADMIN", 0, 10);

		// then
		assertThat(response.content()).hasSize(1);
		assertThat(response.content().get(0).role()).isEqualTo(AdminRole.ADMIN);
	}

	@Test
	@DisplayName("관리자 정보 조회 성공")
	void getAdminInfoTest() {
		// given
		Long adminId = 1L;

		given(adminRepository.findById(adminId)).willReturn(Optional.of(admin));

		// when
		AdminInfoResponse response = adminService.getAdminInfo(adminId);

		// then
		assertThat(response.name()).isEqualTo(admin.getName());
		assertThat(response.email()).isEqualTo(admin.getEmail());
	}

	@Test
	@DisplayName("관리자 정보 조회 실패 - 존재하지 않는 관리자")
	void getAdminInfoFailAdminNotFound() {
		// given
		Long adminId = 999L;

		given(adminRepository.findById(adminId)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> adminService.getAdminInfo(adminId))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("해당하는 관리자를 찾을 수 없습니다.");
	}

	@Test
	@DisplayName("관리자 ID로 조회 성공")
	void getAdminByIdTest() {
		// given
		Long adminId = 1L;

		given(adminRepository.findById(adminId)).willReturn(Optional.of(admin));

		// when
		AdminInfoResponse response = adminService.getAdminById(adminId);

		// then
		assertThat(response.name()).isEqualTo(admin.getName());
		assertThat(response.email()).isEqualTo(admin.getEmail());
	}

	@Test
	@DisplayName("관리자 ID로 조회 실패 - 존재하지 않는 관리자")
	void getAdminByIdFailAdminNotFound() {
		// given
		Long adminId = 999L;

		given(adminRepository.findById(adminId)).willReturn(Optional.empty());

		// when
		assertThatThrownBy(() -> adminService.getAdminById(adminId))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("해당하는 관리자를 찾을 수 없습니다.");
	}

	@Test
	@DisplayName("관리자 삭제 성공")
	void deleteAdminTest() {
		// given
		Long adminId = 1L;

		given(adminRepository.findById(adminId)).willReturn(Optional.of(admin));

		// when
		adminService.deleteAdmin(adminId);

		// then
		verify(adminRepository).delete(admin);
		verify(kafkaProducerService).publishMessage(eq("ADMIN_DELETED"), any());
	}

	@Test
	@DisplayName("관리자 삭제 실패 - 존재하지 않는 관리자")
	void deleteAdminFailAdminNotFound() {
		// given
		Long adminId = 999L;

		given(adminRepository.findById(adminId)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> adminService.deleteAdmin(adminId))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("ID " + adminId + "에 해당하는 관리자를 찾을 수 없습니다.");
	}
}
