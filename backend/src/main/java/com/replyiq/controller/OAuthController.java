package com.replyiq.controller;

import com.replyiq.service.GoogleBusinessService;
import com.replyiq.service.GoogleOAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/oauth2")
@RequiredArgsConstructor
public class OAuthController {

    private final GoogleOAuthService googleOAuthService;
    private final GoogleBusinessService googleBusinessService;

    @Value("${replyiq.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @GetMapping("/google/authorize")
    public ResponseEntity<Map<String, String>> authorize(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        String url = googleOAuthService.buildAuthorizationUrl(userId);
        return ResponseEntity.ok(Map.of("url", url));
    }

    @GetMapping("/callback/google")
    public ResponseEntity<Void> callback(
            @RequestParam("code") String code,
            @RequestParam("state") String state) {

        // Verify the signed state token — rejects tampered or expired values
        Long userId = googleOAuthService.verifyStateAndGetUserId(state);

        googleOAuthService.exchangeCodeForTokens(code, userId);
        var locations = googleBusinessService.fetchAndStoreLocations(userId);

        log.info("OAuth callback complete — {} locations connected for user {}", locations.size(), userId);

        // Redirect to frontend dashboard after successful connection
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(frontendUrl + "/dashboard?connected=" + locations.size()));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}
