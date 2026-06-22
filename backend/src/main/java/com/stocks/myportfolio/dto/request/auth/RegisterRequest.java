package com.stocks.myportfolio.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 16, max = 20) String password,
        @NotBlank String firstName,
        String lastName,
        @NotBlank String securityQuestion1,
        @NotBlank String securityAnswer1,
        @NotBlank String securityQuestion2,
        @NotBlank String securityAnswer2
) {}
