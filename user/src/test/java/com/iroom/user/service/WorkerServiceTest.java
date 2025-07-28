package com.iroom.user.service;

import com.iroom.user.dto.request.*;
import com.iroom.user.dto.response.*;
import com.iroom.user.entity.Worker;
import com.iroom.user.enums.BloodType;
import com.iroom.user.enums.Gender;
import com.iroom.user.enums.WorkerRole;
import com.iroom.user.jwt.JwtTokenProvider;
import com.iroom.user.repository.WorkerRepository;

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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class WorkerServiceTest {

	@Mock
	private WorkerRepository workerRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtTokenProvider jwtTokenProvider;

	@InjectMocks
	private WorkerService workerService;

	private Worker worker;
	private Pageable pageable;
	private Page<Worker> workerPage;

	@BeforeEach
	void setUp() {
		worker = Worker.builder()
			.name("worker")
			.email("worker@example.com")
			.password("encodedPassword")
			.phone("010-1234-5678")
			.role(WorkerRole.WORKER)
			.bloodType(BloodType.A)
			.gender(Gender.MALE)
			.age(30)
			.weight(70.0f)
			.height(175.0f)
			.jobTitle("팀장")
			.occupation("철근공")
			.department("건설부")
			.faceImageUrl("face.jpg")
			.build();

		Worker worker2 = Worker.builder()
			.name("worker2")
			.email("worker2@example.com")
			.password("encodedPassword")
			.phone("010-9876-5432")
			.role(WorkerRole.WORKER)
			.bloodType(BloodType.B)
			.gender(Gender.FEMALE)
			.age(25)
			.weight(60.0f)
			.height(165.0f)
			.jobTitle("반장")
			.occupation("목공")
			.department("인테리어부")
			.faceImageUrl("face2.jpg")
			.build();

		pageable = PageRequest.of(0, 10);
		workerPage = new PageImpl<>(List.of(worker, worker2), pageable, 2);
	}

	@Test
	@DisplayName("근로자 등록 성공")
	void registerWorkerTest() {
		// given
		WorkerRegisterRequest request = new WorkerRegisterRequest(
			"worker", "worker@example.com", "password", "010-1234-5678",
			BloodType.A, Gender.MALE, 30, 70.0f, 175.2f, "팀장", "철근공", "건설팀", "face.jpg"
		);

		given(workerRepository.existsByEmail(request.email())).willReturn(false);
		given(workerRepository.save(any(Worker.class))).willReturn(worker);

		// when
		WorkerRegisterResponse response = workerService.registerWorker(request);

		// then
		assertThat(response.name()).isEqualTo(worker.getName());
		assertThat(response.email()).isEqualTo(worker.getEmail());
	}

	@Test
	@DisplayName("근로자 등록 실패 - 이메일 중복")
	void registerWorkerFailEmailExists() {
		// given
		WorkerRegisterRequest request = new WorkerRegisterRequest(
			"worker", "worker@example.com", "password", "010-1234-5678",
			BloodType.A, Gender.MALE, 30, 70.0f, 175.2f, "팀장", "철근공", "건설팀", "face.jpg"
		);

		given(workerRepository.existsByEmail(request.email())).willReturn(true);

		// when & then
		assertThatThrownBy(() -> workerService.registerWorker(request))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("이미 사용 중인 이메일입니다.");
	}

	@Test
	@DisplayName("근로자 로그인 성공")
	void loginTest() {
		// given
		LoginRequest request = new LoginRequest("worker@example.com", "password");
		String token = "jwt-token";

		given(workerRepository.findByEmail(request.email())).willReturn(Optional.of(worker));
		given(passwordEncoder.matches(request.password(), worker.getPassword())).willReturn(true);
		given(jwtTokenProvider.createWorkerToken(worker)).willReturn(token);

		// when
		LoginResponse response = workerService.login(request);

		// then
		assertThat(response.token()).isEqualTo(token);
	}

	@Test
	@DisplayName("근로자 로그인 실패 - 존재하지 않는 이메일")
	void loginFailEmailNotExists() {
		// given
		LoginRequest request = new LoginRequest("noexistent@example.com", "password");

		given(workerRepository.findByEmail(request.email())).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> workerService.login(request))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("가입되지 않은 이메일입니다.");
	}

	@Test
	@DisplayName("근로자 로그인 실패 - 잘못된 비밀번호")
	void loginFailWrongPassword() {
		// given
		LoginRequest request = new LoginRequest("worker@exmaple.com", "wrongpassword");

		given(workerRepository.findByEmail(request.email())).willReturn(Optional.of(worker));
		given(passwordEncoder.matches(request.password(), worker.getPassword())).willReturn(false);

		// when & then
		assertThatThrownBy(() -> workerService.login(request))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("잘못된 비밀번호입니다.");
	}

	@Test
	@DisplayName("근로자 정보 수정 성공")
	void updateWorkerInfoTest() {
		// given
		Long workerId = 1L;
		WorkerUpdateInfoRequest request = new WorkerUpdateInfoRequest(
			"updatedworker",
			"updeatedworker@example.com",
			"010-1234-5678",
			BloodType.A,
			Gender.MALE,
			30,
			70.0f,
			175.0f,
			"팀장",
			"철근공",
			"건설부",
			"face.jpg");

		given(workerRepository.findById(workerId)).willReturn(Optional.of(worker));
		given(workerRepository.existsByEmail(request.email())).willReturn(false);

		// when
		WorkerUpdateResponse response = workerService.updateWorkerInfo(workerId, request);

		// then
		assertThat(response.name()).isEqualTo(request.name());
		assertThat(response.email()).isEqualTo(request.email());
	}

	@Test
	@DisplayName("근로자 정보 수정 실패 - 존재하지 않는 근로자")
	void updateWorkerInfoFailWorkerNotFound() {
		// given
		Long workerId = 999L;
		WorkerUpdateInfoRequest request = new WorkerUpdateInfoRequest(
			"updatedworker",
			"updeatedworker@example.com",
			"010-1234-5678",
			BloodType.A,
			Gender.MALE,
			30,
			70.0f,
			175.0f,
			"팀장",
			"철근공",
			"건설부",
			"face.jpg");

		given(workerRepository.findById(workerId)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> workerService.updateWorkerInfo(workerId, request))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("해당하는 근로자를 찾을 수 없습니다.");
	}

	@Test
	@DisplayName("근로자 정보 수정 실패 - 이메일 중복")
	void updateWorkerInfoFailEmailExists() {
		// given
		Long workerId = 1L;
		WorkerUpdateInfoRequest request = new WorkerUpdateInfoRequest(
			"updatedworker",
			"updeatedworker@example.com",
			"010-1234-5678",
			BloodType.A,
			Gender.MALE,
			30,
			70.0f,
			175.0f,
			"팀장",
			"철근공",
			"건설부",
			"face.jpg");

		given(workerRepository.findById(workerId)).willReturn(Optional.of(worker));
		given(workerRepository.existsByEmail(request.email())).willReturn(true);

		// when & then
		assertThatThrownBy(() -> workerService.updateWorkerInfo(workerId, request))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("이미 사용 중인 이메일입니다.");
	}
}
