package com.iroom.user.config;

import com.iroom.user.admin.entity.Admin;
import com.iroom.user.admin.enums.AdminRole;
import com.iroom.user.admin.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataLoader implements ApplicationRunner {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Admin superAdmin = Admin.builder()
                .name("Super Admin")
                .email("admin@iroom.com")
                .password(passwordEncoder.encode("admin123!"))
                .phone("010-0000-0000")
                .role(AdminRole.SUPER_ADMIN)
                .build();

        adminRepository.save(superAdmin);
        System.out.println("SUPER_ADMIN 계정이 생성되었습니다.");
        System.out.println("Email: admin@iroom.com");
        System.out.println("Password: admin123!");
    }
}