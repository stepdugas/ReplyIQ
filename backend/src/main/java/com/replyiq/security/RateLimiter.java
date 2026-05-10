package com.replyiq.security;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimiter {

    private final ConcurrentHashMap<String, List<Instant>> attempts = new ConcurrentHashMap<>();

    /**
     * Checks if the given key has exceeded the max attempts within the window.
     * Returns true if the request is allowed, false if rate limited.
     */
    public synchronized boolean isAllowed(String key, int maxAttempts, long windowSeconds) {
        Instant now = Instant.now();
        Instant cutoff = now.minusSeconds(windowSeconds);

        List<Instant> timestamps = attempts.computeIfAbsent(key, k -> new ArrayList<>());

        // Remove expired entries
        timestamps.removeIf(t -> t.isBefore(cutoff));

        if (timestamps.size() >= maxAttempts) {
            return false;
        }

        timestamps.add(now);
        return true;
    }

    /**
     * Periodically clean up stale entries to prevent memory leaks.
     */
    public synchronized void cleanup() {
        Instant oneHourAgo = Instant.now().minusSeconds(3600);
        attempts.entrySet().removeIf(entry -> {
            entry.getValue().removeIf(t -> t.isBefore(oneHourAgo));
            return entry.getValue().isEmpty();
        });
    }
}
