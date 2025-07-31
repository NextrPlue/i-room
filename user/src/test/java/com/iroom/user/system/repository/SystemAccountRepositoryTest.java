package com.iroom.user.system.repository;

import com.iroom.user.system.entity.SystemAccount;
import com.iroom.modulecommon.enums.SystemRole;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class SystemAccountRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SystemAccountRepository systemAccountRepository;

    @Test
    @DisplayName("API Key로 시스템 계정 조회")
    void findByApiKeyTest() {
        // given
        SystemAccount systemAccount = SystemAccount.builder()
                .name("TEST_ENTRANCE_SYSTEM")
                .apiKey("test-api-key-123")
                .role(SystemRole.ENTRANCE_SYSTEM)
                .build();

        entityManager.persistAndFlush(systemAccount);

        // when
        Optional<SystemAccount> result = systemAccountRepository.findByApiKey("test-api-key-123");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("TEST_ENTRANCE_SYSTEM");
        assertThat(result.get().getApiKey()).isEqualTo("test-api-key-123");
        assertThat(result.get().getRole()).isEqualTo(SystemRole.ENTRANCE_SYSTEM);
    }

    @Test
    @DisplayName("이름으로 시스템 계정 조회")
    void existsByNameTrueTest() {
        // given
        SystemAccount systemAccount = SystemAccount.builder()
                .name("WORKER_SYSTEM")
                .apiKey("worker-api-key")
                .role(SystemRole.WORKER_SYSTEM)
                .build();

        entityManager.persistAndFlush(systemAccount);

        // when
        Boolean exists = systemAccountRepository.existsByName("WORKER_SYSTEM");

        // then
        assertThat(exists).isTrue();
    }
}