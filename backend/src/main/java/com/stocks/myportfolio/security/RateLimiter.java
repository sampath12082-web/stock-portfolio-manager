package com.stocks.myportfolio.security;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

@Component
public class RateLimiter {

    private record RateEntry(AtomicInteger count, long windowStart) {}

    private final ConcurrentHashMap<String, RateEntry> attempts = new ConcurrentHashMap<>();

    public boolean isAllowed(String key, int maxAttempts, long windowMs) {
        long now = System.currentTimeMillis();
        attempts.compute(key, (k, entry) -> {
            if (entry == null || now - entry.windowStart() > windowMs) {
                return new RateEntry(new AtomicInteger(1), now);
            }
            entry.count().incrementAndGet();
            return entry;
        });
        return attempts.get(key).count().get() <= maxAttempts;
    }

    public void reset(String key) {
        attempts.remove(key);
    }
}
