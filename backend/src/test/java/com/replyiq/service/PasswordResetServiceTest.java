package com.replyiq.service;

import com.replyiq.model.User;
import com.replyiq.repository.UserRepository;
import com.replyiq.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private PasswordResetService passwordResetService;

    @Test
    void requestReset_sendsEmailForExistingUser() {
        User user = User.builder()
                .id(1L)
                .email("user@test.com")
                .name("Test User")
                .passwordHash("hash")
                .build();

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(eq(1L), eq("user@test.com"), eq("password-reset"), anyLong()))
                .thenReturn("reset-token");

        passwordResetService.requestReset("user@test.com");

        verify(emailService).sendPasswordResetEmail(eq("user@test.com"), contains("reset-token"));
    }

    @Test
    void requestReset_doesNothingForNonExistentUser() {
        when(userRepository.findByEmail("nobody@test.com")).thenReturn(Optional.empty());

        // Should NOT throw an exception (prevents email enumeration)
        assertDoesNotThrow(() -> passwordResetService.requestReset("nobody@test.com"));

        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());
    }

    @Test
    void resetPassword_withValidToken_updatesPassword() {
        User user = User.builder()
                .id(1L)
                .email("user@test.com")
                .name("Test User")
                .passwordHash("old-hash")
                .build();

        when(jwtUtil.isTokenValid("valid-token")).thenReturn(true);
        when(jwtUtil.getPurposeFromToken("valid-token")).thenReturn("password-reset");
        when(jwtUtil.getUserIdFromToken("valid-token")).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newpassword123")).thenReturn("$2a$new-hash");

        passwordResetService.resetPassword("valid-token", "newpassword123");

        assertEquals("$2a$new-hash", user.getPasswordHash());
        verify(userRepository).save(user);
        verify(emailService).sendPasswordResetConfirmation("user@test.com");
    }

    @Test
    void resetPassword_withInvalidToken_throwsException() {
        when(jwtUtil.isTokenValid("invalid-token")).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> passwordResetService.resetPassword("invalid-token", "newpass"));
        assertEquals("Reset link is invalid or has expired", ex.getMessage());

        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPassword_withWrongPurposeToken_throwsException() {
        when(jwtUtil.isTokenValid("login-token")).thenReturn(true);
        when(jwtUtil.getPurposeFromToken("login-token")).thenReturn("login");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> passwordResetService.resetPassword("login-token", "newpass"));
        assertEquals("Reset link is invalid or has expired", ex.getMessage());

        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPassword_withValidTokenButUserNotFound_throwsException() {
        when(jwtUtil.isTokenValid("orphan-token")).thenReturn(true);
        when(jwtUtil.getPurposeFromToken("orphan-token")).thenReturn("password-reset");
        when(jwtUtil.getUserIdFromToken("orphan-token")).thenReturn(999L);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> passwordResetService.resetPassword("orphan-token", "newpass"));
        assertEquals("Reset link is invalid or has expired", ex.getMessage());
    }
}
