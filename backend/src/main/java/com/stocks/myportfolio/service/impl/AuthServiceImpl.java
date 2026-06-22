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
    private final com.stocks.myportfolio.service.RsaKeyService rsaKeyService;

    public AuthServiceImpl(UserRepository userRepository, OtpTokenRepository otpTokenRepository,
            PasswordEncoder passwordEncoder, JwtService jwtService, EmailService emailService,
            com.stocks.myportfolio.service.RsaKeyService rsaKeyService) {
        this.userRepository = userRepository;
        this.otpTokenRepository = otpTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailService = emailService;
        this.rsaKeyService = rsaKeyService;
    }

    private String decryptIfEncrypted(String value) {
        if (value == null) return null;
        return rsaKeyService.decrypt(value);
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
        String plainPassword = decryptIfEncrypted(request.password());
        validatePassword(plainPassword);
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already registered: " + request.email());
        }

        User user = new User();
        user.setEmail(request.email().toLowerCase().trim());
        user.setPasswordHash(passwordEncoder.encode(plainPassword));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setRole("ROLE_USER");
        user.setStatus("ACTIVE");
        user.setEmailVerified(false);
        user.setSecurityQuestion1(request.securityQuestion1());
        user.setSecurityAnswer1Hash(passwordEncoder.encode(request.securityAnswer1().toLowerCase().trim()));
        user.setSecurityQuestion2(request.securityQuestion2());
        user.setSecurityAnswer2Hash(passwordEncoder.encode(request.securityAnswer2().toLowerCase().trim()));

        User saved = userRepository.save(user);
        sendOtp(saved.getEmail(), "REGISTRATION");

        return toUserResponse(saved);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email().toLowerCase().trim())
                .orElseThrow(() -> new ValidationException("Invalid email or password"));

        String loginPassword = decryptIfEncrypted(request.password());
        if (!passwordEncoder.matches(loginPassword, user.getPasswordHash())) {
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
        // No-op: security questions flow handled by getSecurityQuestions + verifySecurityAnswers
    }

    public java.util.Map<String, String> getSecurityQuestions(String email) {
        User user = userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new ValidationException("No account found with this email"));
        if (user.getSecurityQuestion1() == null) {
            throw new ValidationException("No security questions set for this account");
        }
        return java.util.Map.of(
                "securityQuestion1", user.getSecurityQuestion1(),
                "securityQuestion2", user.getSecurityQuestion2());
    }

    public void verifySecurityAnswers(String email, String answer1, String answer2) {
        User user = userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new ValidationException("No account found"));
        if (!passwordEncoder.matches(answer1.toLowerCase().trim(), user.getSecurityAnswer1Hash())) {
            throw new ValidationException("Security answer 1 is incorrect");
        }
        if (!passwordEncoder.matches(answer2.toLowerCase().trim(), user.getSecurityAnswer2Hash())) {
            throw new ValidationException("Security answer 2 is incorrect");
        }
        sendOtp(email.toLowerCase().trim(), "PASSWORD_RESET");
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
        String newPw = decryptIfEncrypted(request.newPassword());
        validatePassword(newPw);
        user.setPasswordHash(passwordEncoder.encode(newPw));
        userRepository.save(user);
    }

    @Override
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String currentPw = decryptIfEncrypted(request.currentPassword());
        if (!passwordEncoder.matches(currentPw, user.getPasswordHash())) {
            throw new ValidationException("Current password is incorrect");
        }

        String changePw = decryptIfEncrypted(request.newPassword());
        validatePassword(changePw);
        user.setPasswordHash(passwordEncoder.encode(changePw));
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
