package com.replyiq.service;

import com.replyiq.dto.AuthResponse;
import com.replyiq.dto.LoginRequest;
import com.replyiq.dto.SignupRequest;
import com.replyiq.model.User;
import com.replyiq.repository.UserRepository;
import com.replyiq.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final NurtureSequenceService nurtureSequenceService;

    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .subscriptionStatus("trialing")
                .trialEndDate(LocalDateTime.now().plusDays(7))
                .build();

        user = userRepository.save(user);
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), "login");

        log.info("New user signed up — {}", user.getEmail());

        // Send welcome email (day 0 of nurture sequence)
        try {
            nurtureSequenceService.sendWelcomeEmail(user);
        } catch (Exception e) {
            log.warn("Failed to send welcome email to {}: {}", user.getEmail(), e.getMessage());
        }

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .name(user.getName())
                .subscriptionStatus(user.getSubscriptionStatus())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), "login");

        log.info("User logged in — {}", user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .name(user.getName())
                .subscriptionStatus(user.getSubscriptionStatus())
                .build();
    }
}
