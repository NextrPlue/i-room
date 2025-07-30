package com.iroom.user.repository;

import com.iroom.user.entity.Worker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class WorkerRepositoryTest {

    @Autowired
    private WorkerRepository workerRepository;

    private Pageable pageable;

    @BeforeEach
    void setUp() {
        workerRepository.deleteAll();

        Worker worker = Worker.builder()
                .name("worker")
                .email("worker@example.com")
                .password("!test123")
                .build();

        workerRepository.save(worker);

        pageable = PageRequest.of(0, 10);
    }

    @Test
    @DisplayName("이메일로 근로자 존재 여부 확인")
    void existsByEmail() {
        // when
        boolean exists = workerRepository.existsByEmail("worker@example.com");
        boolean notExists = workerRepository.existsByEmail("wrong@example.com");

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("이메일로 근로자 찾기")
    void findByEmail() {
        // when
        Optional<Worker> foundWorker = workerRepository.findByEmail("worker@example.com");
        Optional<Worker> notFoundWorker = workerRepository.findByEmail("wrong@example.com");

        // then
        assertThat(foundWorker).isPresent();
        assertThat(foundWorker.get().getEmail()).isEqualTo("worker@example.com");
        assertThat(notFoundWorker).isNotPresent();
    }

    @Test
    @DisplayName("이름으로 근로자 검색")
    void findByNameContaining() {
        // when
        Page<Worker> result = workerRepository.findByNameContaining("worker", pageable);
        Page<Worker> emptyResult = workerRepository.findByNameContaining("nonexistent", pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("worker");
        assertThat(emptyResult.getContent()).isEmpty();
    }

    @Test
    @DisplayName("이메일로 근로자 검색")
    void findByEmailContaining() {
        // when
        Page<Worker> result = workerRepository.findByEmailContaining("worker", pageable);
        Page<Worker> emptyResult = workerRepository.findByEmailContaining("nonexistent", pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("worker@example.com");
        assertThat(emptyResult.getContent()).isEmpty();
    }
}
