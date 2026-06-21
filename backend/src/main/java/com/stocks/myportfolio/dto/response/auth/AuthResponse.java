package com.stocks.myportfolio.dto.response.auth;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        UserResponse user
) {}
