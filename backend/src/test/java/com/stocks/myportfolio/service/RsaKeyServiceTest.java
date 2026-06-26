package com.stocks.myportfolio.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RsaKeyServiceTest {

    private RsaKeyService rsaKeyService;

    @BeforeEach
    void setup() {
        rsaKeyService = new RsaKeyService();
        rsaKeyService.init();
    }

    @Test
    void publicKey_isPem() {
        String pem = rsaKeyService.getPublicKeyPem();
        assertTrue(pem.contains("BEGIN PUBLIC KEY"));
        assertTrue(pem.contains("END PUBLIC KEY"));
    }

    @Test
    void publicKey_consistent_acrossCalls() {
        String pem1 = rsaKeyService.getPublicKeyPem();
        String pem2 = rsaKeyService.getPublicKeyPem();
        assertEquals(pem1, pem2);
    }

    @Test
    void decrypt_invalidInput_returnsOriginal() {
        String input = "not-valid-base64!!!";
        assertEquals(input, rsaKeyService.decrypt(input));
    }

    @Test
    void decrypt_wrongCiphertext_returnsOriginal() {
        String input = "aGVsbG8gd29ybGQ=";
        assertEquals(input, rsaKeyService.decrypt(input));
    }
}
