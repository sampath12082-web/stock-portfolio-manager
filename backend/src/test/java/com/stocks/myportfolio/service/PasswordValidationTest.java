package com.stocks.myportfolio.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class PasswordValidationTest {

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]{16,20}$");

    @ParameterizedTest
    @ValueSource(strings = {
            "Admin@1234567890*",
            "ValidPass@123456",
            "MySecure#Pass1234",
    })
    void validPasswords(String password) {
        assertTrue(PASSWORD_PATTERN.matcher(password).matches(), password + " should be valid");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Short@123",
            "nouppercase@12345",
            "NOLOWERCASE@12345",
            "NoDigitsHere@abcde",
            "NoSpecialChar12345a",
            "TooLongPassword@12345678",
    })
    void invalidPasswords(String password) {
        assertFalse(PASSWORD_PATTERN.matcher(password).matches(), password + " should be invalid");
    }

    @Test
    void exactlyMinLength_16chars() {
        assertTrue(PASSWORD_PATTERN.matcher("Abcdefgh@1234567").matches());
    }

    @Test
    void exactlyMaxLength_20chars() {
        assertTrue(PASSWORD_PATTERN.matcher("Abcdefghij@123456789").matches());
    }

    @Test
    void nullPassword() {
        assertThrows(NullPointerException.class, () -> PASSWORD_PATTERN.matcher(null));
    }
}
