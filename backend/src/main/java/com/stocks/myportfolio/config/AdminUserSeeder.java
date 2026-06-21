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

    @Value("${admin.default-password:Admin@123}")
    private String adminDefaultPassword;

    public AdminUserSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.existsByEmail("sampath12082@gmail.com")) {
            log.info("Admin user already exists");
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
        log.info("Admin user created: sampath12082@gmail.com (change password on first login)");
    }
}
