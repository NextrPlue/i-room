package com.iroom.management.repository;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.iroom.management.entity.WorkerEdu;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class WorkerEduRepositoryTest {

	@Autowired
	private WorkerEduRepository workerEduRepository;

	private WorkerEdu workerEdu1;
	private Pageable pageable;

	@BeforeEach
	void setUp() {
		workerEduRepository.deleteAll();

		workerEdu1 = WorkerEdu.builder()
			.workerId(1L)
			.name("근로자1")
			.certUrl("https://example.com/cert1.jpg")
			.eduDate(LocalDate.now())
			.build();

		WorkerEdu workerEdu2 = WorkerEdu.builder()
			.workerId(2L)
			.name("근로자2")
			.certUrl("https://example.com/cert2.jpg")
			.eduDate(LocalDate.now())
			.build();

		workerEduRepository.save(workerEdu1);
		workerEduRepository.save(workerEdu2);

		pageable = PageRequest.of(0, 10);
	}

	@Test
	@DisplayName("교육이력 저장 및 조회 성공")
	void saveAndFindById() {
		// when
		Optional<WorkerEdu> result = workerEduRepository.findById(workerEdu1.getId());

		// then
		assertThat(result).isPresent();
		assertThat(result.get().getName()).isEqualTo("근로자1");
	}

	@Test
	@DisplayName("교육이력 전체 조회 성공")
	void findAllWorkerEdu() {
		// when
		Page<WorkerEdu> page = workerEduRepository.findAll(pageable);

		// then
		assertThat(page.getContent()).hasSize(2);
	}

	@Test
	@DisplayName("교육이력 삭제 성공")
	void deleteWorkerEdu() {
		// when
		workerEduRepository.delete(workerEdu1);

		// then
		Optional<WorkerEdu> result = workerEduRepository.findById(workerEdu1.getId());
		assertThat(result).isNotPresent();
	}
}