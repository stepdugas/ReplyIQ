package com.replyiq.service;

import com.replyiq.dto.AuthResponse;
import com.replyiq.dto.LoginRequest;
import com.replyiq.dto.SignupRequest;
import com.replyiq.model.User;
import com.replyiq.repository.UserRepository;
import com.replyiq.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private NurtureSequenceService nurtureSequenceService;

    @InjectMocks
    private AuthService authService;

    @Test
    void signup_withValidData_createsUserWithHashedPasswordAndTrialingStatus() {
        SignupRequest request = new SignupRequest();
        request.setName("Jane Doe");
        request.setEmail("jane@test.com");
        request.setPassword("securepassword");

        when(userRepository.existsByEmail("jane@test.com")).thenReturn(false);
        when(passwordEncoder.encode("securepassword")).thenReturn("$2a$hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(jwtUtil.generateToken(eq(1L), eq("jane@test.com"), eq("login"))).thenReturn("jwt-token");

        AuthResponse response = authService.signup(request);

        assertEquals("jwt-token", response.getToken());
        assertEquals("jane@test.com", response.getEmail());
        assertEquals("Jane Doe", response.getName());
        assertEquals("trialing", response.getSubscriptionStatus());

        // Verify password was hashed before saving
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("$2a$hashed", userCaptor.getValue().getPasswordHash());
        assertEquals("trialing", userCaptor.getValue().getSubscriptionStatus());
        assertNotNull(userCaptor.getValue().getTrialEndDate());
    }

    @Test
    void signup_withDuplicateEmail_throwsIllegalArgumentException() {
        SignupRequest request = new SignupRequest();
        request.setName("Jane");
        request.setEmail("existing@test.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.signup(request));
        assertEquals("Email already registered", ex.getMessage());

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_withCorrectCredentials_returnsAuthResponseWithToken() {
        LoginRequest request = new LoginRequest();
        request.setEmail("jane@test.com");
        request.setPassword("correctpassword");

        User user = User.builder()
                .id(5L)
                .email("jane@test.com")
                .name("Jane Doe")
                .passwordHash("$2a$hashed")
                .subscriptionStatus("active")
                .build();

        when(userRepository.findByEmail("jane@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correctpassword", "$2a$hashed")).thenReturn(true);
        when(jwtUtil.generateToken(5L, "jane@test.com", "login")).thenReturn("login-jwt");

        AuthResponse response = authService.login(request);

        assertEquals("login-jwt", response.getToken());
        assertEquals("jane@test.com", response.getEmail());
        assertEquals("Jane Doe", response.getName());
        assertEquals("active", response.getSubscriptionStatus());
    }

    @Test
    void login_withWrongPassword_throwsIllegalArgumentException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("jane@test.com");
        request.setPassword("wrongpassword");

        User user = User.builder()
                .id(5L)
                .email("jane@test.com")
                .name("Jane Doe")
                .passwordHash("$2a$hashed")
                .subscriptionStatus("active")
                .build();

        when(userRepository.findByEmail("jane@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "$2a$hashed")).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.login(request));
        assertEquals("Invalid email or password", ex.getMessage());
    }

    @Test
    void login_withNonExistentEmail_throwsIllegalArgumentException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("nobody@test.com");
        request.setPassword("anypassword");

        when(userRepository.findByEmail("nobody@test.com")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.login(request));
        assertEquals("Invalid email or password", ex.getMessage());
    }

    @Test
    void signup_sendsWelcomeEmail() {
        SignupRequest request = new SignupRequest();
        request.setName("New User");
        request.setEmail("new@test.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$hash2");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(2L);
            return u;
        });
        when(jwtUtil.generateToken(eq(2L), eq("new@test.com"), eq("login"))).thenReturn("token");

        authService.signup(request);

        verify(nurtureSequenceService).sendWelcomeEmail(any(User.class));
    }
}
