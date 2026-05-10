package com.replyiq.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.replyiq.model.OAuthToken;
import com.replyiq.model.User;
import com.replyiq.repository.OAuthTokenRepository;
import com.replyiq.repository.UserRepository;
import com.replyiq.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GoogleOAuthService {

    private final OAuthTokenRepository oauthTokenRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${replyiq.google.redirect-uri:http://localhost:8080/api/oauth2/callback/google}")
    private String redirectUri;

    private static final String AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String SCOPES = String.join(" ",
            "openid",
            "email",
            "profile",
            "https://www.googleapis.com/auth/business.manage"
    );

    public String buildAuthorizationUrl(Long userId) {
        // Sign the state parameter so the callback can verify it wasn't tampered with
        String signedState = jwtUtil.generateToken(userId, "oauth-state");
        return AUTH_URL
                + "?client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&response_type=code"
                + "&scope=" + SCOPES.replace(" ", "%20")
                + "&access_type=offline"
                + "&prompt=consent"
                + "&state=" + signedState;
    }

    /**
     * Verifies the signed state parameter and extracts the user ID.
     * Throws if the state is invalid or expired.
     */
    public Long verifyStateAndGetUserId(String state) {
        if (!jwtUtil.isTokenValid(state)) {
            throw new IllegalArgumentException("Invalid or expired OAuth state");
        }
        return jwtUtil.getUserIdFromToken(state);
    }

    public OAuthToken exchangeCodeForTokens(String code, Long userId) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        ResponseEntity<String> response = restTemplate.exchange(
                TOKEN_URL, HttpMethod.POST,
                new HttpEntity<>(params, headers),
                String.class
        );

        try {
            JsonNode json = objectMapper.readTree(response.getBody());
            String accessToken = json.get("access_token").asText();
            String refreshToken = json.has("refresh_token") ? json.get("refresh_token").asText() : null;
            int expiresIn = json.get("expires_in").asInt();

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            OAuthToken token = oauthTokenRepository.findByUserId(userId)
                    .orElse(OAuthToken.builder().user(user).build());

            token.setAccessToken(accessToken);
            if (refreshToken != null) {
                token.setRefreshToken(refreshToken);
            }
            token.setExpiresAt(LocalDateTime.now().plusSeconds(expiresIn));

            token = oauthTokenRepository.save(token);
            System.out.println("SUCCESS: Google OAuth tokens saved for user " + userId);
            return token;

        } catch (Exception e) {
            throw new RuntimeException("Failed to exchange authorization code: " + e.getMessage(), e);
        }
    }

    public String refreshAccessToken(OAuthToken token) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("refresh_token", token.getRefreshToken());
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("grant_type", "refresh_token");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        ResponseEntity<String> response = restTemplate.exchange(
                TOKEN_URL, HttpMethod.POST,
                new HttpEntity<>(params, headers),
                String.class
        );

        try {
            JsonNode json = objectMapper.readTree(response.getBody());
            String newAccessToken = json.get("access_token").asText();
            int expiresIn = json.get("expires_in").asInt();

            token.setAccessToken(newAccessToken);
            token.setExpiresAt(LocalDateTime.now().plusSeconds(expiresIn));
            oauthTokenRepository.save(token);

            return newAccessToken;
        } catch (Exception e) {
            throw new RuntimeException("Failed to refresh access token: " + e.getMessage(), e);
        }
    }

    public String getValidAccessToken(Long userId) {
        OAuthToken token = oauthTokenRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("No Google account connected"));

        if (token.getExpiresAt().isBefore(LocalDateTime.now().plusMinutes(5))) {
            return refreshAccessToken(token);
        }
        return token.getAccessToken();
    }
}
