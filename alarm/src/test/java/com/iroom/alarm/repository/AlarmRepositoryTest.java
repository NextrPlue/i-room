package com.iroom.alarm.repository;

import com.iroom.alarm.entity.Alarm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AlarmRepositoryTest {

	@Autowired
	private AlarmRepository alarmRepository;

	private Alarm alarm1;
	private Alarm alarm2;

	@BeforeEach
	void setUp() {
		alarmRepository.deleteAll();

		LocalDateTime now = LocalDateTime.now();
		alarm1 = Alarm.builder()
			.workerId(1L)
			.occurredAt(now.minusMinutes(30))
			.incidentId(101L)
			.incidentType("위험요소")
			.incidentDescription("작업자 침입 감지")
			.latitude(37.5665)
			.longitude(126.9780)
			.imageUrl("http://example.com/image1.jpg")
			.build();

		alarm2 = Alarm.builder()
			.workerId(2L)
			.occurredAt(now.minusMinutes(10))
			.incidentId(102L)
			.incidentType("건강 이상")
			.incidentDescription("심박수 급증")
			.latitude(37.5651)
			.longitude(126.9895)
			.build();

		alarmRepository.save(alarm1);
		alarmRepository.save(alarm2);
	}

	@Test
	@DisplayName("알림 저장 및 ID로 조회 성공 - IDENTITY 전략 확인")
	void saveAndFindById() {
		// when
		Optional<Alarm> result = alarmRepository.findById(alarm1.getId());

		// then
		assertThat(result).isPresent();
		assertThat(result.get().getId()).isNotNull();
		assertThat(result.get().getWorkerId()).isEqualTo(1L);
		assertThat(result.get().getCreatedAt()).isNotNull();
		assertThat(result.get().getLatitude()).isEqualTo(37.5665);
		assertThat(result.get().getLongitude()).isEqualTo(126.9780);
		assertThat(result.get().getImageUrl()).isEqualTo("http://example.com/image1.jpg");
	}

	@Test
	@DisplayName("workerId로 알림 리스트 조회 성공")
	void findByWorkerIdOrderByOccurredAtDesc() {
		// when
		Pageable pageable = PageRequest.of(0, 10);
		Page<Alarm> result = alarmRepository.findByWorkerIdOrderByOccurredAtDesc(1L, pageable);

		// then
		assertThat(result.getContent()).isNotEmpty();
		assertThat(result.getContent().get(0).getIncidentDescription()).isEqualTo("작업자 침입 감지");
	}

	@Test
	@DisplayName("최근 3시간 이내 알림 조회 성공")
	void findByOccurredAtAfterOrderByOccurredAtDesc() {
		// when
		LocalDateTime threeHoursAgo = LocalDateTime.now().minusHours(3);
		Pageable pageable = PageRequest.of(0, 10);
		Page<Alarm> result = alarmRepository.findByOccurredAtAfterOrderByOccurredAtDesc(threeHoursAgo, pageable);

		// then
		assertThat(result.getContent()).hasSize(2);
		assertThat(result.getContent().get(0).getOccurredAt())
			.isAfter(result.getContent().get(1).getOccurredAt());
	}

	@Test
	@DisplayName("알림 삭제 성공")
	void deleteAlarm() {
		// when
		alarmRepository.delete(alarm1);

		// then
		Optional<Alarm> result = alarmRepository.findById(alarm1.getId());
		assertThat(result).isNotPresent();
	}

	@Test
	@DisplayName("빈 결과 - 존재하지 않는 근로자 ID로 조회")
	void findByWorkerIdOrderByOccurredAtDesc_emptyResult() {
		// when
		Pageable pageable = PageRequest.of(0, 10);
		Page<Alarm> result = alarmRepository.findByWorkerIdOrderByOccurredAtDesc(999L, pageable);

		// then
		assertThat(result.getContent()).isEmpty();
		assertThat(result.getTotalElements()).isEqualTo(0);
	}

	@Test
	@DisplayName("미래 시간으로 조회 - 빈 결과")
	void findByOccurredAtAfterOrderByOccurredAtDesc_futureTime() {
		// when
		LocalDateTime futureTime = LocalDateTime.now().plusHours(1);
		Pageable pageable = PageRequest.of(0, 10);
		Page<Alarm> result = alarmRepository.findByOccurredAtAfterOrderByOccurredAtDesc(futureTime, pageable);

		// then
		assertThat(result.getContent()).isEmpty();
		assertThat(result.getTotalElements()).isEqualTo(0);
	}

	@Test
	@DisplayName("페이지네이션 테스트")
	void paginationTest() {
		// given - 추가 데이터 생성
		for (int i = 3; i <= 12; i++) {
			Alarm additionalAlarm = Alarm.builder()
				.workerId(1L)
				.occurredAt(LocalDateTime.now().minusMinutes(i * 5))
				.incidentId((long)(100 + i))
				.incidentType("테스트 사고")
				.incidentDescription("테스트 사고 " + i)
				.build();
			alarmRepository.save(additionalAlarm);
		}

		// when - 첫 번째 페이지 (size=5)
		Pageable firstPage = PageRequest.of(0, 5);
		Page<Alarm> firstResult = alarmRepository.findByWorkerIdOrderByOccurredAtDesc(1L, firstPage);

		// then
		assertThat(firstResult.getContent()).hasSize(5);
		assertThat(firstResult.getTotalElements()).isEqualTo(11); // alarm1 + 10개 추가
		assertThat(firstResult.getTotalPages()).isEqualTo(3); // 11/5 = 3페이지
		assertThat(firstResult.isFirst()).isTrue();
		assertThat(firstResult.isLast()).isFalse();

		// when - 두 번째 페이지
		Pageable secondPage = PageRequest.of(1, 5);
		Page<Alarm> secondResult = alarmRepository.findByWorkerIdOrderByOccurredAtDesc(1L, secondPage);

		// then
		assertThat(secondResult.getContent()).hasSize(5);
		assertThat(secondResult.isFirst()).isFalse();
		assertThat(secondResult.isLast()).isFalse();
	}
}
