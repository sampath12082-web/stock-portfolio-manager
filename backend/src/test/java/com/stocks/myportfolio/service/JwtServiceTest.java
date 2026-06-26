package com.stocks.myportfolio.service;

import com.stocks.myportfolio.service.impl.JwtServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtServiceImpl jwtService;

    @BeforeEach
    void setup() {
        jwtService = new JwtServiceImpl();
        ReflectionTestUtils.setField(jwtService, "secret",
                "bXlwb3J0Zm9saW8tc2VjcmV0LWtleS1mb3Itand0LXRva2VuLWdlbmVyYXRpb24tMjAyNg==");
        ReflectionTestUtils.setField(jwtService, "accessExpiration", 900000L);
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", 604800000L);
    }

    @Test
    void generateAccessToken_returnsValidJwt() {
        String token = jwtService.generateAccessToken("test@example.com", "ROLE_USER");
        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    void extractEmail_returnsCorrectEmail() {
        String token = jwtService.generateAccessToken("test@example.com", "ROLE_USER");
        String email = jwtService.extractEmail(token);
        assertEquals("test@example.com", email);
    }

    @Test
    void isTokenValid_trueForFreshToken() {
        String token = jwtService.generateAccessToken("test@example.com", "ROLE_USER");
        UserDetails user = User.withUsername("test@example.com").password("").authorities("ROLE_USER").build();
        assertTrue(jwtService.isTokenValid(token, user));
    }

    @Test
    void isTokenValid_falseForWrongEmail() {
        String token = jwtService.generateAccessToken("test@example.com", "ROLE_USER");
        UserDetails other = User.withUsername("other@example.com").password("").authorities("ROLE_USER").build();
        assertFalse(jwtService.isTokenValid(token, other));
    }

    @Test
    void generateRefreshToken_differentFromAccess() {
        String access = jwtService.generateAccessToken("test@example.com", "ROLE_USER");
        String refresh = jwtService.generateRefreshToken("test@example.com");
        assertNotEquals(access, refresh);
    }

    @Test
    void shortKey_padded_doesNotThrow() {
        ReflectionTestUtils.setField(jwtService, "secret", "c2hvcnRrZXk="); // "shortkey" = 8 bytes
        assertDoesNotThrow(() -> jwtService.generateAccessToken("test@example.com", "ROLE_USER"));
    }
}
