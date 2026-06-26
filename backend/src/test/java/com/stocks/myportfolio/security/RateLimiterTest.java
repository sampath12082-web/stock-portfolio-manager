package com.stocks.myportfolio.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RateLimiterTest {

    @Test
    void allowsWithinLimit() {
        RateLimiter limiter = new RateLimiter();
        assertTrue(limiter.isAllowed("test", 3, 60000));
        assertTrue(limiter.isAllowed("test", 3, 60000));
        assertTrue(limiter.isAllowed("test", 3, 60000));
    }

    @Test
    void blocksOverLimit() {
        RateLimiter limiter = new RateLimiter();
        limiter.isAllowed("test", 2, 60000);
        limiter.isAllowed("test", 2, 60000);
        assertFalse(limiter.isAllowed("test", 2, 60000));
    }

    @Test
    void differentKeysIndependent() {
        RateLimiter limiter = new RateLimiter();
        limiter.isAllowed("a", 1, 60000);
        assertTrue(limiter.isAllowed("b", 1, 60000));
    }

    @Test
    void resetClearsCount() {
        RateLimiter limiter = new RateLimiter();
        limiter.isAllowed("test", 1, 60000);
        assertFalse(limiter.isAllowed("test", 1, 60000));
        limiter.reset("test");
        assertTrue(limiter.isAllowed("test", 1, 60000));
    }
}
