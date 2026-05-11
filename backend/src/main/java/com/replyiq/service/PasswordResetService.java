package com.replyiq.service;

import com.replyiq.model.User;
import com.replyiq.repository.UserRepository;
import com.replyiq.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    @Value("${replyiq.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    /**
     * Sends a password reset email if the user exists.
     * Always returns success to prevent email enumeration.
     */
    private static final long ONE_HOUR_MS = 3_600_000L;

    public void requestReset(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            // Generate a short-lived token (1 hour) with password-reset purpose
            String token = jwtUtil.generateToken(user.getId(), user.getEmail(), "password-reset", ONE_HOUR_MS);
            String resetUrl = frontendUrl + "/reset-password?token=" + token;
            emailService.sendPasswordResetEmail(user.getEmail(), resetUrl);
        });

        log.info("Password reset requested for {}", email);
    }

    /**
     * Resets the password using a valid reset token.
     */
    public void resetPassword(String token, String newPassword) {
        if (!jwtUtil.isTokenValid(token)) {
            throw new IllegalArgumentException("Reset link is invalid or has expired");
        }

        // Verify this token was actually issued for password-reset
        String purpose = jwtUtil.getPurposeFromToken(token);
        if (!"password-reset".equals(purpose)) {
            throw new IllegalArgumentException("Reset link is invalid or has expired");
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Reset link is invalid or has expired"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        emailService.sendPasswordResetConfirmation(user.getEmail());
        log.info("Password reset for {}", user.getEmail());
    }
}
