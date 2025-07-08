package io.dashboard.config;

import io.dashboard.model.User;
import io.dashboard.model.UserRole;
import io.dashboard.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminUserInitializer {
    @Bean
    public CommandLineRunner ensureAdminUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String adminUsername = "admin";
            String adminEmail = "admin@admin.com";
            String adminPassword = "adminadmin";
            if (!userRepository.existsByUsername(adminUsername)) {
                User admin = User.builder()
                        .username(adminUsername)
                        .email(adminEmail)
                        .passwordHash(passwordEncoder.encode(adminPassword))
                        .role(UserRole.ADMIN)
                        .build();
                userRepository.save(admin);
                System.out.println("Admin user created: username='admin', password='adminadmin'");
            }
        };
    }
} 