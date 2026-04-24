package com.finance.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.finance.enums.RoleType;
import com.finance.model.Role;
import com.finance.model.User;
import com.finance.repository.RoleRepository;
import com.finance.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class AdminInitializer implements CommandLineRunner {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	public void run(String... args) {

		/*
		 * =============================== ✅ STEP 1: CREATE ROLES AT STARTUP
		 * ===============================
		 */

		for (RoleType role : RoleType.values()) {
			createRoleIfNotExists(role);
		}

		/*
		 * =============================== ✅ STEP 2: CREATE ADMIN USER
		 * ===============================
		 */

		String adminEmail = "admin@financegov.com";

		// If admin already exists → skip creation
		if (userRepository.existsByEmail(adminEmail)) {
			log.info("Admin already exists. Skipping creation.");
			return;
		}

		// Fetch ROLE_ADMIN (now guaranteed to exist)
		Role adminRole = roleRepository.findByRoleName(RoleType.ROLE_ADMIN)
				.orElseThrow(() -> new RuntimeException("ROLE_ADMIN missing in database"));

		// Create Admin User
		User admin = new User();
		admin.setUsername("Admin");
		admin.setEmail(adminEmail);
		admin.setPhone("9146237978");
		admin.setPassword(passwordEncoder.encode("Admin@1234"));
		admin.setRole(adminRole);
		admin.setStatus("ACTIVE");
		admin.setVerified(true);

		userRepository.save(admin);

		log.info("✅ ADMIN CREATED SUCCESSFULLY!");
		log.info("Email: {}", adminEmail);
		log.info("Password: Admin@1234 (change after first login)");
	}

	/*
	 * =============================== ✅ HELPER METHOD
	 * ===============================
	 */

	private void createRoleIfNotExists(RoleType roleType) {
		roleRepository.findByRoleName(roleType).ifPresentOrElse(role -> log.info("Role {} already exists", roleType),
				() -> {
					roleRepository.save(new Role(roleType));
					log.info("✅ Role {} created", roleType);
				});
	}
}
