package com.stocks.myportfolio.controller;

import java.security.Principal;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.stocks.myportfolio.dto.request.auth.*;
import com.stocks.myportfolio.dto.response.auth.AuthResponse;
import com.stocks.myportfolio.dto.response.auth.UserResponse;
import com.stocks.myportfolio.entity.User;
import com.stocks.myportfolio.repository.UserRepository;
import com.stocks.myportfolio.security.RateLimiter;
import com.stocks.myportfolio.service.AuthService;
import com.stocks.myportfolio.service.RsaKeyService;
import com.stocks.myportfolio.service.impl.AuthServiceImpl;

import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthServiceImpl authServiceImpl;
    private final RsaKeyService rsaKeyService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RateLimiter rateLimiter;

    public AuthController(AuthService authService, AuthServiceImpl authServiceImpl, RsaKeyService rsaKeyService,
            UserRepository userRepository, PasswordEncoder passwordEncoder, RateLimiter rateLimiter) {
        this.authService = authService;
        this.authServiceImpl = authServiceImpl;
        this.rsaKeyService = rsaKeyService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.rateLimiter = rateLimiter;
    }

    @GetMapping("/public-key")
    public Map<String, String> getPublicKey() {
        return Map.of("publicKey", rsaKeyService.getPublicKeyPem());
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request, jakarta.servlet.http.HttpServletRequest httpReq) {
        String key = "register:" + httpReq.getRemoteAddr();
        if (!rateLimiter.isAllowed(key, 3, 600000)) {
            throw new com.stocks.myportfolio.common.exception.ValidationException("Too many registration attempts. Try again in 10 minutes.");
        }
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request, jakarta.servlet.http.HttpServletRequest httpReq) {
        String emailKey = "login:" + request.email();
        String ipKey = "login-ip:" + httpReq.getRemoteAddr();
        if (!rateLimiter.isAllowed(emailKey, 5, 300000)) {
            throw new com.stocks.myportfolio.common.exception.ValidationException("Too many login attempts for this account. Try again in 5 minutes.");
        }
        if (!rateLimiter.isAllowed(ipKey, 10, 600000)) {
            throw new com.stocks.myportfolio.common.exception.ValidationException("Too many login attempts from this IP. Try again in 10 minutes.");
        }
        AuthResponse response = authService.login(request);
        rateLimiter.reset(emailKey);
        rateLimiter.reset(ipKey);
        return response;
    }

    @PostMapping("/verify-otp")
    public Map<String, String> verifyOtp(@Valid @RequestBody VerifyOtpRequest request, jakarta.servlet.http.HttpServletRequest httpReq) {
        if (!rateLimiter.isAllowed("otp:" + request.email(), 5, 300000))
            throw new com.stocks.myportfolio.common.exception.ValidationException("Too many OTP attempts. Try again in 5 minutes.");
        authService.verifyOtp(request);
        return Map.of("message", "Email verified successfully");
    }

    @PostMapping("/forgot-password")
    public Map<String, String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request, jakarta.servlet.http.HttpServletRequest httpReq) {
        if (!rateLimiter.isAllowed("forgot:" + httpReq.getRemoteAddr(), 3, 600000))
            throw new com.stocks.myportfolio.common.exception.ValidationException("Too many password reset requests. Try again in 10 minutes.");
        return authServiceImpl.getSecurityQuestions(request.email());
    }

    @PostMapping("/verify-security")
    public Map<String, String> verifySecurity(@RequestBody Map<String, String> body, jakarta.servlet.http.HttpServletRequest httpReq) {
        if (!rateLimiter.isAllowed("security:" + body.get("email"), 3, 300000))
            throw new com.stocks.myportfolio.common.exception.ValidationException("Too many security answer attempts. Try again in 5 minutes.");
        authServiceImpl.verifySecurityAnswers(body.get("email"), body.get("answer1"), body.get("answer2"));
        return Map.of("message", "Security answers verified. OTP sent to your email.");
    }

    @PostMapping("/reset-password")
    public Map<String, String> resetPassword(@Valid @RequestBody ResetPasswordRequest request, jakarta.servlet.http.HttpServletRequest httpReq) {
        if (!rateLimiter.isAllowed("reset:" + httpReq.getRemoteAddr(), 3, 600000))
            throw new com.stocks.myportfolio.common.exception.ValidationException("Too many password reset attempts. Try again in 10 minutes.");
        authService.resetPassword(request);
        return Map.of("message", "Password reset successfully");
    }

    @PostMapping("/change-password")
    public Map<String, String> changePassword(Principal principal,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(principal.getName(), request);
        return Map.of("message", "Password changed successfully");
    }

    @PostMapping("/refresh")
    public AuthResponse refreshToken(@RequestBody Map<String, String> body) {
        return authService.refreshToken(body.get("refreshToken"));
    }

    @PostMapping("/setup-admin")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> setupAdmin(@RequestBody Map<String, String> body, jakarta.servlet.http.HttpServletRequest httpReq) {
        if (!rateLimiter.isAllowed("setup:" + httpReq.getRemoteAddr(), 3, 600000)) {
            throw new com.stocks.myportfolio.common.exception.ValidationException("Too many setup attempts. Try again in 10 minutes.");
        }

        String email = body.get("email");
        String password = body.get("password");
        String firstName = body.getOrDefault("firstName", "Admin");
        String lastName = body.get("lastName");

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            return Map.of("message", "email and password are required");
        }

        long adminCount = userRepository.findAll().stream()
                .filter(u -> "ROLE_ADMIN".equals(u.getRole())).count();

        var existing = userRepository.findByEmail(email);
        if (existing.isPresent()) {
            boolean resetPassword = "true".equalsIgnoreCase(body.get("resetPassword"));
            if (resetPassword) {
                User admin = existing.get();
                admin.setPasswordHash(passwordEncoder.encode(password));
                admin.setEmailVerified(true);
                userRepository.save(admin);
                return Map.of("message", "Admin password reset for: " + email);
            }
            return Map.of("message", "Admin user already exists. Pass resetPassword=true to reset.");
        }

        if (adminCount > 0) {
            return Map.of("message", "An admin already exists. New admin creation is disabled.");
        }

        User admin = new User();
        admin.setEmail(email);
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setFirstName(firstName);
        if (lastName != null) admin.setLastName(lastName);
        admin.setRole("ROLE_ADMIN");
        admin.setStatus("ACTIVE");
        admin.setEmailVerified(true);
        userRepository.save(admin);

        return Map.of("message", "Admin user created: " + email);
    }
}
