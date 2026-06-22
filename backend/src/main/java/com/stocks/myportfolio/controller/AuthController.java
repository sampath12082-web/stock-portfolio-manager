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

    public AuthController(AuthService authService, AuthServiceImpl authServiceImpl, RsaKeyService rsaKeyService,
            UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.authService = authService;
        this.authServiceImpl = authServiceImpl;
        this.rsaKeyService = rsaKeyService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/public-key")
    public Map<String, String> getPublicKey() {
        return Map.of("publicKey", rsaKeyService.getPublicKeyPem());
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/verify-otp")
    public Map<String, String> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        authService.verifyOtp(request);
        return Map.of("message", "Email verified successfully");
    }

    @PostMapping("/forgot-password")
    public Map<String, String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return authServiceImpl.getSecurityQuestions(request.email());
    }

    @PostMapping("/verify-security")
    public Map<String, String> verifySecurity(@RequestBody Map<String, String> body) {
        authServiceImpl.verifySecurityAnswers(body.get("email"), body.get("answer1"), body.get("answer2"));
        return Map.of("message", "Security answers verified. OTP sent to your email.");
    }

    @PostMapping("/reset-password")
    public Map<String, String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
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
    public Map<String, String> setupAdmin(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");
        String firstName = body.getOrDefault("firstName", "Admin");
        String lastName = body.get("lastName");

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            return Map.of("message", "email and password are required");
        }

        if (userRepository.findByEmail(email).isPresent()) {
            return Map.of("message", "Admin user already exists. No action taken.");
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
