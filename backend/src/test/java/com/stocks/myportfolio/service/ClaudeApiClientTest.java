package com.stocks.myportfolio.service;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class ClaudeApiClientTest {

    @Test
    void isAvailable_falseWhenNoKey() {
        ClaudeApiClient client = new ClaudeApiClient();
        ReflectionTestUtils.setField(client, "apiKey", "");
        assertFalse(client.isAvailable());
    }

    @Test
    void isAvailable_falseWhenNull() {
        ClaudeApiClient client = new ClaudeApiClient();
        ReflectionTestUtils.setField(client, "apiKey", null);
        assertFalse(client.isAvailable());
    }

    @Test
    void isAvailable_trueWhenSet() {
        ClaudeApiClient client = new ClaudeApiClient();
        ReflectionTestUtils.setField(client, "apiKey", "sk-test-key");
        assertTrue(client.isAvailable());
    }
}
