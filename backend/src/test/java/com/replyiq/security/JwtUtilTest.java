package com.replyiq.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    // A 256-bit (32-byte) secret for HMAC-SHA256
    private static final String TEST_SECRET = "test-secret-key-that-is-at-least-32-bytes-long!!";
    private static final long DEFAULT_EXPIRATION_MS = 86_400_000L; // 24 hours

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(TEST_SECRET, DEFAULT_EXPIRATION_MS);
    }

    @Test
    void generateToken_containsCorrectClaims() {
        String token = jwtUtil.generateToken(42L, "test@example.com", "login");

        assertEquals(42L, jwtUtil.getUserIdFromToken(token));
        assertEquals("test@example.com", jwtUtil.getEmailFromToken(token));
        assertEquals("login", jwtUtil.getPurposeFromToken(token));
    }

    @Test
    void generateToken_defaultPurposeIsLogin() {
        String token = jwtUtil.generateToken(1L, "user@test.com");

        assertEquals("login", jwtUtil.getPurposeFromToken(token));
    }

    @Test
    void isTokenValid_returnsTrueForValidToken() {
        String token = jwtUtil.generateToken(1L, "user@test.com", "login");

        assertTrue(jwtUtil.isTokenValid(token));
    }

    @Test
    void isTokenValid_returnsFalseForExpiredToken() {
        // Generate a token that expired 1 ms ago
        String token = jwtUtil.generateToken(1L, "user@test.com", "login", 0L);

        // Token with 0ms expiration should be expired immediately (or within ms)
        // Small race window, so we also accept valid in rare cases - but typically false
        // Use a negative-like approach: generate with 1ms, then sleep
        JwtUtil shortLivedUtil = new JwtUtil(TEST_SECRET, 1L);
        String shortToken = shortLivedUtil.generateToken(1L, "user@test.com");
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertFalse(jwtUtil.isTokenValid(shortToken));
    }

    @Test
    void isTokenValid_returnsFalseForGarbageToken() {
        assertFalse(jwtUtil.isTokenValid("not.a.real.token"));
    }

    @Test
    void isTokenValid_returnsFalseForTamperedToken() {
        String token = jwtUtil.generateToken(1L, "user@test.com", "login");
        String tampered = token + "tampered";

        assertFalse(jwtUtil.isTokenValid(tampered));
    }

    @Test
    void getPurposeFromToken_detectsDifferentPurposes() {
        String loginToken = jwtUtil.generateToken(1L, "u@t.com", "login");
        String resetToken = jwtUtil.generateToken(1L, "u@t.com", "password-reset");
        String oauthToken = jwtUtil.generateToken(1L, "u@t.com", "oauth-state");
        String unsubToken = jwtUtil.generateToken(1L, "u@t.com", "unsubscribe");

        assertEquals("login", jwtUtil.getPurposeFromToken(loginToken));
        assertEquals("password-reset", jwtUtil.getPurposeFromToken(resetToken));
        assertEquals("oauth-state", jwtUtil.getPurposeFromToken(oauthToken));
        assertEquals("unsubscribe", jwtUtil.getPurposeFromToken(unsubToken));
    }

    @Test
    void generateToken_customExpirationWorks() {
        long oneHour = 3_600_000L;
        String token = jwtUtil.generateToken(5L, "custom@test.com", "password-reset", oneHour);

        assertTrue(jwtUtil.isTokenValid(token));
        assertEquals(5L, jwtUtil.getUserIdFromToken(token));
        assertEquals("password-reset", jwtUtil.getPurposeFromToken(token));
    }

    @Test
    void constructor_throwsForBlankSecret() {
        assertThrows(IllegalStateException.class, () -> new JwtUtil("", 86400000L));
    }

    @Test
    void constructor_throwsForPlaceholderSecret() {
        assertThrows(IllegalStateException.class, () -> new JwtUtil("placeholder", 86400000L));
    }

    @Test
    void tokenFromDifferentSecret_isInvalid() {
        JwtUtil otherUtil = new JwtUtil("another-secret-key-that-is-at-least-32-bytes!!", DEFAULT_EXPIRATION_MS);
        String token = otherUtil.generateToken(1L, "user@test.com", "login");

        assertFalse(jwtUtil.isTokenValid(token));
    }
}
