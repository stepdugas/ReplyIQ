package com.replyiq.service;

import com.replyiq.model.User;
import com.replyiq.repository.UserRepository;
import com.replyiq.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
    public void requestReset(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            // Generate a short-lived token (1 hour) using the existing JWT util
            String token = jwtUtil.generateToken(user.getId(), "password-reset");
            String resetUrl = frontendUrl + "/reset-password?token=" + token;
            emailService.sendPasswordResetEmail(user.getEmail(), resetUrl);
        });

        System.out.println("SUCCESS: Password reset requested for " + email);
    }

    /**
     * Resets the password using a valid reset token.
     */
    public void resetPassword(String token, String newPassword) {
        if (!jwtUtil.isTokenValid(token)) {
            throw new IllegalArgumentException("Reset link is invalid or has expired");
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Reset link is invalid or has expired"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        emailService.sendPasswordResetConfirmation(user.getEmail());
        System.out.println("SUCCESS: Password reset for " + user.getEmail());
    }
}
