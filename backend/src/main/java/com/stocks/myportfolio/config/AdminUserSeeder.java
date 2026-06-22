package com.stocks.myportfolio.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.stocks.myportfolio.entity.User;
import com.stocks.myportfolio.repository.UserRepository;

@Component
public class AdminUserSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminUserSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private String adminDefaultPassword = "Admin@1234567890*";

    public AdminUserSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        var existing = userRepository.findByEmail("sampath12082@gmail.com");
        if (existing.isPresent()) {
            User admin = existing.get();
            admin.setPasswordHash(passwordEncoder.encode(adminDefaultPassword));
            admin.setEmailVerified(true);
            userRepository.save(admin);
            log.info("Admin user password reset to default");
            return;
        }

        User admin = new User();
        admin.setEmail("sampath12082@gmail.com");
        admin.setPasswordHash(passwordEncoder.encode(adminDefaultPassword));
        admin.setFirstName("Sampat Kumar");
        admin.setLastName("Asealu");
        admin.setRole("ROLE_ADMIN");
        admin.setStatus("ACTIVE");
        admin.setEmailVerified(true);

        userRepository.save(admin);
        log.info("Admin user created: sampath12082@gmail.com");
    }
}
