package com.stocks.myportfolio.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank @Email String email,
        @NotBlank String otpCode,
        @NotBlank @Size(min = 6) String newPassword
) {}
