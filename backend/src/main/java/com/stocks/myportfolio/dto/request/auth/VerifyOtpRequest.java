package com.stocks.myportfolio.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record VerifyOtpRequest(
        @NotBlank @Email String email,
        @NotBlank String otpCode
) {}
