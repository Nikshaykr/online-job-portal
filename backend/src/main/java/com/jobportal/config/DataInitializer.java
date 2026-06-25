package com.jobportal.config;

import com.jobportal.model.Role;
import com.jobportal.model.User;
import com.jobportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        // Inside DataInitializer.java
        Optional<User> existingAdmin = userRepository.findByEmail("admin@jobportal.com");

        if (existingAdmin.isEmpty()) {
            User admin = new User();
            admin.setName("Admin");
            admin.setEmail("admin@jobportal.com");
            admin.setPassword(passwordEncoder.encode("Admin@123")); // Properly hashed!
            admin.setRole(Role.valueOf("ADMIN"));
            userRepository.save(admin);
            System.out.println("✅ Admin account seeded successfully!");
        } else {
            User admin = existingAdmin.get();
            // FIX: If it exists but the password isn't BCrypt, update it!
            if (!admin.getPassword().startsWith("$2a$")) {
                admin.setPassword(passwordEncoder.encode("Admin@123"));
                userRepository.save(admin);
                System.out.println("🔄 Admin password updated to valid BCrypt format!");
            }
        }

        // 1. Seed DEFAULT SYSTEM ADMIN account
        if (!userRepository.existsByEmail("admin@portal.com")) {
            User admin = new User();
            admin.setName("System Admin");
            admin.setEmail("admin@portal.com");
            // Encodes the password securely using your configured BCrypt bean
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
            System.out.println("✔ Default Admin account initialized (admin@portal.com / admin123)");
        }

        // 2. Seed DEFAULT DEMO EMPLOYER account (Optional, but great for quick testing)
        if (!userRepository.existsByEmail("employer@portal.com")) {
            User employer = new User();
            employer.setName("Tech Corp HR");
            employer.setEmail("employer@portal.com");
            employer.setPassword(passwordEncoder.encode("employer123"));
            employer.setRole(Role.EMPLOYER);
            userRepository.save(employer);
            System.out.println("✔ Default Employer account initialized (employer@portal.com / employer123)");
        }

        // 3. Seed DEFAULT DEMO SEEKER account (Optional, but great for quick testing)
        if (!userRepository.existsByEmail("seeker@portal.com")) {
            User seeker = new User();
            seeker.setName("John Doe");
            seeker.setEmail("seeker@portal.com");
            seeker.setPassword(passwordEncoder.encode("seeker123"));
            seeker.setRole(Role.SEEKER);
            userRepository.save(seeker);
            System.out.println("✔ Default Seeker account initialized (seeker@portal.com / seeker123)");
        }
    }
}