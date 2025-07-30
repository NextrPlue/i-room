package com.iroom.user.repository;

import com.iroom.user.admin.entity.Admin;
import com.iroom.user.admin.enums.AdminRole;
import com.iroom.user.admin.repository.AdminRepository;
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
public class AdminRepositoryTest {

    @Autowired
    private AdminRepository adminRepository;

    private Pageable pageable;

    @BeforeEach
    void setUp() {
        adminRepository.deleteAll();

        Admin admin1 = Admin.builder()
                .name("superadmin")
                .email("superadmin@example.com")
                .password("!test123")
                .role(AdminRole.SUPER_ADMIN)
                .build();

        Admin admin2 = Admin.builder()
                .name("admin")
                .email("admin@example.com")
                .password("!test123")
                .role(AdminRole.ADMIN)
                .build();

        Admin admin3 = Admin.builder()
                .name("reader")
                .email("reader@example.com")
                .password("!test123")
                .role(AdminRole.READER)
                .build();

        adminRepository.save(admin1);
        adminRepository.save(admin2);
        adminRepository.save(admin3);

        pageable = PageRequest.of(0, 10);
    }

    @Test
    @DisplayName("이메일로 관리자 존재 여부 확인")
    void existsByEmail() {
        // when
        boolean exists = adminRepository.existsByEmail("admin@example.com");
        boolean notExists = adminRepository.existsByEmail("wrong@example.com");

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("이메일로 관리자 찾기")
    void findByEmail() {
        // when
        Optional<Admin> foundAdmin = adminRepository.findByEmail("admin@example.com");
        Optional<Admin> notFoundAdmin = adminRepository.findByEmail("wrong@example.com");

        // then
        assertThat(foundAdmin).isPresent();
        assertThat(foundAdmin.get().getEmail()).isEqualTo("admin@example.com");
        assertThat(notFoundAdmin).isNotPresent();
    }

    @Test
    @DisplayName("이름으로 관리자 검색")
    void findByNameContaining() {
        // when
        Page<Admin> result = adminRepository.findByNameContaining("admin", pageable);
        Page<Admin> emptyResult = adminRepository.findByNameContaining("nonexistent", pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getName()).isEqualTo("superadmin");
        assertThat(emptyResult.getContent()).isEmpty();
    }

    @Test
    @DisplayName("이메일로 관리자 검색")
    void findByEmailContaining() {
        // when
        Page<Admin> result = adminRepository.findByEmailContaining("admin", pageable);
        Page<Admin> emptyResult = adminRepository.findByEmailContaining("nonexistent", pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("superadmin@example.com");
        assertThat(emptyResult.getContent()).isEmpty();
    }

    @Test
    @DisplayName("역할로 관리자 검색")
    void findByRole() {
        // when
        Page<Admin> superAdminResult = adminRepository.findByRole(AdminRole.SUPER_ADMIN, pageable);
        Page<Admin> adminResult = adminRepository.findByRole(AdminRole.ADMIN, pageable);
        Page<Admin> readerResult = adminRepository.findByRole(AdminRole.READER, pageable);

        // then
        assertThat(superAdminResult.getContent()).hasSize(1);
        assertThat(superAdminResult.getContent().get(0).getRole()).isEqualTo(AdminRole.SUPER_ADMIN);
        assertThat(adminResult.getContent()).hasSize(1);
        assertThat(adminResult.getContent().get(0).getRole()).isEqualTo(AdminRole.ADMIN);
        assertThat(readerResult.getContent()).hasSize(1);
        assertThat(readerResult.getContent().get(0).getRole()).isEqualTo(AdminRole.READER);
    }
}
