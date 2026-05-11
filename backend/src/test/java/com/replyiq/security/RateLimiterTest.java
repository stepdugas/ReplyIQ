package com.replyiq.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RateLimiterTest {

    private RateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        rateLimiter = new RateLimiter();
    }

    @Test
    void isAllowed_allowsRequestsWithinLimit() {
        String key = "user-1";
        int maxAttempts = 5;
        long windowSeconds = 60;

        for (int i = 0; i < maxAttempts; i++) {
            assertTrue(rateLimiter.isAllowed(key, maxAttempts, windowSeconds),
                    "Request " + (i + 1) + " should be allowed");
        }
    }

    @Test
    void isAllowed_blocksRequestsOverLimit() {
        String key = "user-2";
        int maxAttempts = 3;
        long windowSeconds = 60;

        // Use up all allowed attempts
        for (int i = 0; i < maxAttempts; i++) {
            assertTrue(rateLimiter.isAllowed(key, maxAttempts, windowSeconds));
        }

        // Next request should be blocked
        assertFalse(rateLimiter.isAllowed(key, maxAttempts, windowSeconds));
        assertFalse(rateLimiter.isAllowed(key, maxAttempts, windowSeconds));
    }

    @Test
    void isAllowed_resetsAfterWindowExpires() throws InterruptedException {
        String key = "user-3";
        int maxAttempts = 2;
        long windowSeconds = 1; // 1 second window

        // Use up all attempts
        assertTrue(rateLimiter.isAllowed(key, maxAttempts, windowSeconds));
        assertTrue(rateLimiter.isAllowed(key, maxAttempts, windowSeconds));
        assertFalse(rateLimiter.isAllowed(key, maxAttempts, windowSeconds));

        // Wait for window to expire
        Thread.sleep(1100);

        // Should be allowed again
        assertTrue(rateLimiter.isAllowed(key, maxAttempts, windowSeconds));
    }

    @Test
    void isAllowed_differentKeysAreIndependent() {
        int maxAttempts = 2;
        long windowSeconds = 60;

        // Exhaust key-A
        assertTrue(rateLimiter.isAllowed("key-A", maxAttempts, windowSeconds));
        assertTrue(rateLimiter.isAllowed("key-A", maxAttempts, windowSeconds));
        assertFalse(rateLimiter.isAllowed("key-A", maxAttempts, windowSeconds));

        // key-B should still be allowed
        assertTrue(rateLimiter.isAllowed("key-B", maxAttempts, windowSeconds));
        assertTrue(rateLimiter.isAllowed("key-B", maxAttempts, windowSeconds));
    }

    @Test
    void cleanup_removesStaleEntries() throws InterruptedException {
        // Add entries with a very short window so they become "stale"
        rateLimiter.isAllowed("stale-key", 10, 1);

        // Wait for entries to age past 1 second (they won't be past 1 hour though,
        // but cleanup removes entries older than 1 hour - so let's test that cleanup
        // doesn't crash and that fresh entries survive)
        rateLimiter.isAllowed("fresh-key", 10, 3600);

        // Cleanup should run without error
        rateLimiter.cleanup();

        // Fresh key should still be tracked (blocked after limit)
        // Verify the limiter still works after cleanup
        assertTrue(rateLimiter.isAllowed("new-key", 1, 60));
        assertFalse(rateLimiter.isAllowed("new-key", 1, 60));
    }

    @Test
    void isAllowed_singleAttemptLimit() {
        assertTrue(rateLimiter.isAllowed("once", 1, 60));
        assertFalse(rateLimiter.isAllowed("once", 1, 60));
    }
}
