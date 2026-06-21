package com.stocks.myportfolio.dto.response.auth;

public record UserResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        String phone,
        String role,
        String status,
        boolean emailVerified
) {}
