package com.stocks.myportfolio.service;

import java.security.*;
import java.util.Base64;

import javax.crypto.Cipher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class RsaKeyService {

    private static final Logger log = LoggerFactory.getLogger(RsaKeyService.class);

    private KeyPair keyPair;

    @PostConstruct
    public void init() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            keyPair = generator.generateKeyPair();
            log.info("RSA 2048-bit key pair generated");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate RSA key pair", e);
        }
    }

    public String getPublicKeyPem() {
        byte[] encoded = keyPair.getPublic().getEncoded();
        String base64 = Base64.getEncoder().encodeToString(encoded);
        return "-----BEGIN PUBLIC KEY-----\n" + base64 + "\n-----END PUBLIC KEY-----";
    }

    public String decrypt(String encryptedBase64) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedBase64));
            return new String(decrypted);
        } catch (Exception e) {
            // If decryption fails, assume plain text (for backward compatibility)
            return encryptedBase64;
        }
    }
}
