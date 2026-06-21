package com.stocks.myportfolio.service.impl;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stocks.myportfolio.common.exception.DuplicateResourceException;
import com.stocks.myportfolio.common.exception.ResourceNotFoundException;
import com.stocks.myportfolio.common.exception.ValidationException;
import com.stocks.myportfolio.dto.request.auth.*;
import com.stocks.myportfolio.dto.response.auth.AuthResponse;
import com.stocks.myportfolio.dto.response.auth.UserResponse;
import com.stocks.myportfolio.entity.OtpToken;
import com.stocks.myportfolio.entity.User;
import com.stocks.myportfolio.repository.OtpTokenRepository;
import com.stocks.myportfolio.repository.UserRepository;
import com.stocks.myportfolio.service.AuthService;
import com.stocks.myportfolio.service.EmailService;
import com.stocks.myportfolio.service.JwtService;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final OtpTokenRepository otpTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    public AuthServiceImpl(UserRepository userRepository, OtpTokenRepository otpTokenRepository,
            PasswordEncoder passwordEncoder, JwtService jwtService, EmailService emailService) {
        this.userRepository = userRepository;
        this.otpTokenRepository = otpTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }

    private static final java.util.regex.Pattern PASSWORD_PATTERN =
            java.util.regex.Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]{16,20}$");

    private void validatePassword(String password) {
        if (password == null || !PASSWORD_PATTERN.matcher(password).matches()) {
            throw new ValidationException(
                    "Password must be 16-20 characters with at least 1 uppercase, 1 lowercase, 1 digit, and 1 special character (!@#$%^&*)");
        }
    }

    @Override
    public UserResponse register(RegisterRequest request) {
        validatePassword(request.password());
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already registered: " + request.email());
        }

        User user = new User();
        user.setEmail(request.email().toLowerCase().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setRole("ROLE_USER");
        user.setStatus("ACTIVE");
        user.setEmailVerified(false);

        User saved = userRepository.save(user);
        sendOtp(saved.getEmail(), "REGISTRATION");

        return toUserResponse(saved);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email().toLowerCase().trim())
                .orElseThrow(() -> new ValidationException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ValidationException("Invalid email or password");
        }

        if (!user.isEmailVerified()) {
            throw new ValidationException("Email not verified. Please check your inbox for the OTP.");
        }

        if (!"ACTIVE".equals(user.getStatus())) {
            throw new ValidationException("Account is " + user.getStatus().toLowerCase());
        }

        String accessToken = jwtService.generateAccessToken(user.getEmail(), user.getRole());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        return new AuthResponse(accessToken, refreshToken, toUserResponse(user));
    }

    @Override
    public void verifyOtp(VerifyOtpRequest request) {
        OtpToken otp = otpTokenRepository
                .findTopByEmailAndPurposeAndUsedFalseOrderByCreatedAtDesc(
                        request.email().toLowerCase().trim(), "REGISTRATION")
                .orElseThrow(() -> new ValidationException("No pending OTP found"));

        validateOtp(otp, request.otpCode());

        User user = userRepository.findByEmail(request.email().toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setEmailVerified(true);
        userRepository.save(user);
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        if (!userRepository.existsByEmail(request.email().toLowerCase().trim())) {
            return;
        }
        sendOtp(request.email().toLowerCase().trim(), "PASSWORD_RESET");
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        OtpToken otp = otpTokenRepository
                .findTopByEmailAndPurposeAndUsedFalseOrderByCreatedAtDesc(
                        request.email().toLowerCase().trim(), "PASSWORD_RESET")
                .orElseThrow(() -> new ValidationException("No pending OTP found"));

        validateOtp(otp, request.otpCode());

        User user = userRepository.findByEmail(request.email().toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        validatePassword(request.newPassword());
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Override
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new ValidationException("Current password is incorrect");
        }

        validatePassword(request.newPassword());
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        String email = jwtService.extractEmail(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ValidationException("Invalid refresh token"));

        String newAccessToken = jwtService.generateAccessToken(user.getEmail(), user.getRole());
        return new AuthResponse(newAccessToken, refreshToken, toUserResponse(user));
    }

    private void sendOtp(String email, String purpose) {
        String code = String.format("%06d", new Random().nextInt(999999));

        OtpToken otp = new OtpToken();
        otp.setEmail(email);
        otp.setOtpCode(code);
        otp.setPurpose(purpose);
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        otpTokenRepository.save(otp);

        String subject = "REGISTRATION".equals(purpose)
                ? "MyPortfolio — Verify your email"
                : "MyPortfolio — Password reset OTP";
        emailService.sendOtpEmail(email, subject, code);
    }

    private void validateOtp(OtpToken otp, String code) {
        if (otp.isExpired()) {
            throw new ValidationException("OTP has expired");
        }
        if (!otp.getOtpCode().equals(code)) {
            throw new ValidationException("Invalid OTP");
        }
        otp.setUsed(true);
        otpTokenRepository.save(otp);
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getFirstName(),
                user.getLastName(), user.getPhone(), user.getRole(), user.getStatus(),
                user.isEmailVerified());
    }
}
