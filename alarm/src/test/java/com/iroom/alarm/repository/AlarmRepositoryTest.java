package com.iroom.alarm.repository;

import com.iroom.alarm.entity.Alarm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;
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

		alarm1 = Alarm.builder()
			.workerId(1L)
			.incidentId(101L)
			.incidentType("위험요소")
			.incidentDescription("작업자 침입 감지")
			.build();

		alarm2 = Alarm.builder()
			.workerId(2L)
			.incidentId(102L)
			.incidentType("건강 이상")
			.incidentDescription("심박수 급증")
			.build();

		alarmRepository.save(alarm1);
		alarmRepository.save(alarm2);
	}

	@Test
	@DisplayName("알림 저장 및 ID로 조회 성공")
	void saveAndFindById() {
		// when
		Optional<Alarm> result = alarmRepository.findById(alarm1.getId());

		// then
		assertThat(result).isPresent();
		assertThat(result.get().getWorkerId()).isEqualTo(1L);
	}

	@Test
	@DisplayName("workerId로 알림 리스트 조회 성공")
	void findByWorkerIdOrderByOccuredAtDesc() {
		// when
		List<Alarm> result = alarmRepository.findByWorkerIdOrderByOccuredAtDesc(1L);

		// then
		assertThat(result).isNotEmpty();
		assertThat(result.get(0).getIncidentDescription()).isEqualTo("작업자 침입 감지");
	}

	@Test
	@DisplayName("최근 3시간 이내 알림 조회 성공")
	void findByOccuredAtAfterOrderByOccuredAtDesc() {
		// when
		LocalDateTime threeHoursAgo = LocalDateTime.now().minusHours(3);
		List<Alarm> result = alarmRepository.findByOccuredAtAfterOrderByOccuredAtDesc(threeHoursAgo);

		// then
		assertThat(result).hasSize(2);
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
}
