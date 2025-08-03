package com.iroom.user.system.service;

import com.iroom.modulecommon.exception.CustomException;
import com.iroom.modulecommon.exception.ErrorCode;
import com.iroom.user.common.jwt.JwtTokenProvider;
import com.iroom.user.system.dto.request.SystemAuthRequest;
import com.iroom.user.system.dto.response.SystemAuthResponse;
import com.iroom.user.system.entity.SystemAccount;
import com.iroom.modulecommon.enums.SystemRole;
import com.iroom.user.system.repository.SystemAccountRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class SystemAccountServiceTest {

	@Mock
	private SystemAccountRepository systemAccountRepository;

	@Mock
	private JwtTokenProvider jwtTokenProvider;

	@InjectMocks
	private SystemAccountService systemAccountService;

	private SystemAccount systemAccount;
	private String testApiKey;
	private String testToken;

	@BeforeEach
	void setUp() {
		testApiKey = "test-api-key-123";
		testToken = "jwt-system-token";

		systemAccount = SystemAccount.builder()
			.name("TEST_ENTRANCE_SYSTEM")
			.apiKey(testApiKey)
			.role(SystemRole.ENTRANCE_SYSTEM)
			.build();
	}

	@Test
	@DisplayName("시스템 인증 성공")
	void authenticateTest() {
		// given
		SystemAuthRequest request = new SystemAuthRequest(testApiKey);

		given(systemAccountRepository.findByApiKey(testApiKey)).willReturn(Optional.of(systemAccount));
		given(jwtTokenProvider.createSystemToken(systemAccount)).willReturn(testToken);

		// when
		SystemAuthResponse response = systemAccountService.authenticate(request);

		// then
		assertThat(response.token()).isEqualTo(testToken);
	}

	@Test
	@DisplayName("시스템 인증 실패 - 유효하지 않은 API Key")
	void authenticateFailInvalidApiKey() {
		// given
		String invalidApiKey = "invalid-api-key";
		SystemAuthRequest request = new SystemAuthRequest(invalidApiKey);

		given(systemAccountRepository.findByApiKey(invalidApiKey)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> systemAccountService.authenticate(request))
			.isInstanceOf(CustomException.class)
			.satisfies(ex -> {
				CustomException customException = (CustomException)ex;
				assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.USER_INVALID_API_KEY);
			});
	}

	@Test
	@DisplayName("시스템 인증 성공 - WORKER_SYSTEM 역할")
	void authenticateWorkerSystemTest() {
		// given
		SystemAccount workerSystem = SystemAccount.builder()
			.name("TEST_WORKER_SYSTEM")
			.apiKey("worker-api-key")
			.role(SystemRole.WORKER_SYSTEM)
			.build();

		SystemAuthRequest request = new SystemAuthRequest("worker-api-key");

		given(systemAccountRepository.findByApiKey("worker-api-key")).willReturn(Optional.of(workerSystem));
		given(jwtTokenProvider.createSystemToken(workerSystem)).willReturn(testToken);

		// when
		SystemAuthResponse response = systemAccountService.authenticate(request);

		// then
		assertThat(response.token()).isEqualTo(testToken);
	}

	@Test
	@DisplayName("시스템 인증 성공 - EQUIPMENT_SYSTEM 역할")
	void authenticateEquipmentSystemTest() {
		// given
		SystemAccount equipmentSystem = SystemAccount.builder()
			.name("TEST_EQUIPMENT_SYSTEM")
			.apiKey("equipment-api-key")
			.role(SystemRole.EQUIPMENT_SYSTEM)
			.build();

		SystemAuthRequest request = new SystemAuthRequest("equipment-api-key");

		given(systemAccountRepository.findByApiKey("equipment-api-key")).willReturn(Optional.of(equipmentSystem));
		given(jwtTokenProvider.createSystemToken(equipmentSystem)).willReturn(testToken);

		// when
		SystemAuthResponse response = systemAccountService.authenticate(request);

		// then
		assertThat(response.token()).isEqualTo(testToken);
	}
}