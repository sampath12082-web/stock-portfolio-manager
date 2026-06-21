package com.stocks.myportfolio.service;

import com.stocks.myportfolio.dto.request.auth.*;
import com.stocks.myportfolio.dto.response.auth.AuthResponse;
import com.stocks.myportfolio.dto.response.auth.UserResponse;

public interface AuthService {

    UserResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    void verifyOtp(VerifyOtpRequest request);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    void changePassword(String email, ChangePasswordRequest request);

    AuthResponse refreshToken(String refreshToken);
}
