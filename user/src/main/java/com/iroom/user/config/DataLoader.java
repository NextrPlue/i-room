package com.iroom.user.config;

import com.iroom.user.admin.entity.Admin;
import com.iroom.modulecommon.enums.AdminRole;
import com.iroom.user.admin.repository.AdminRepository;
import com.iroom.user.system.entity.SystemAccount;
import com.iroom.modulecommon.enums.SystemRole;
import com.iroom.user.system.repository.SystemAccountRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DataLoader implements ApplicationRunner {

	private final AdminRepository adminRepository;
	private final PasswordEncoder passwordEncoder;
	private final SystemAccountRepository systemAccountRepository;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		if (!adminRepository.existsByRole(AdminRole.SUPER_ADMIN)) {
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

		if (!systemAccountRepository.existsByName("Entrance System")) {
			SystemAccount system = SystemAccount.builder()
				.name("Entrance System")
				.apiKey("entrance-system-api-key-" + UUID.randomUUID())
				.role(SystemRole.ENTRANCE_SYSTEM)
				.build();

			systemAccountRepository.save(system);
			System.out.println("Entrance System 계정이 생성되었습니다.");
			System.out.println("API Key: " + system.getApiKey());
		}

		if (!systemAccountRepository.existsByName("Worker System")) {
			SystemAccount system = SystemAccount.builder()
				.name("Worker System")
				.apiKey("worker-system-api-key-" + UUID.randomUUID())
				.role(SystemRole.WORKER_SYSTEM)
				.build();

			systemAccountRepository.save(system);
			System.out.println("Worker System 계정이 생성되었습니다.");
			System.out.println("API Key: " + system.getApiKey());
		}

		if (!systemAccountRepository.existsByName("Equipment System")) {
			SystemAccount system = SystemAccount.builder()
				.name("Equipment System")
				.apiKey("equipment-system-api-key-" + UUID.randomUUID())
				.role(SystemRole.EQUIPMENT_SYSTEM)
				.build();

			systemAccountRepository.save(system);
			System.out.println("Equipment System 계정이 생성되었습니다.");
			System.out.println("API Key: " + system.getApiKey());
		}

		if (!systemAccountRepository.existsByName("PPE System")) {
			SystemAccount system = SystemAccount.builder()
				.name("PPE System")
				.apiKey("ppe-system-api-key-" + UUID.randomUUID())
				.role(SystemRole.PPE_SYSTEM)
				.build();

			systemAccountRepository.save(system);
			System.out.println("PPE System 계정이 생성되었습니다.");
			System.out.println("API Key: " + system.getApiKey());
		}
	}
}