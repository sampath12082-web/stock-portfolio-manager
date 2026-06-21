package com.stocks.myportfolio.service;

import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {

    String generateAccessToken(String email, String role);

    String generateRefreshToken(String email);

    String extractEmail(String token);

    boolean isTokenValid(String token, UserDetails userDetails);
}
