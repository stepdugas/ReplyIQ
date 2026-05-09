package com.replyiq.controller;

import com.replyiq.service.GoogleBusinessService;
import com.replyiq.service.GoogleOAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/oauth2")
@RequiredArgsConstructor
public class OAuthController {

    private final GoogleOAuthService googleOAuthService;
    private final GoogleBusinessService googleBusinessService;

    /**
     * Returns the Google OAuth URL the frontend should redirect the user to.
     */
    @GetMapping("/google/authorize")
    public ResponseEntity<Map<String, String>> authorize(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        String url = googleOAuthService.buildAuthorizationUrl(userId);
        return ResponseEntity.ok(Map.of("url", url));
    }

    /**
     * Google redirects here after user grants permission.
     * Exchanges the code for tokens, fetches locations, then redirects to dashboard.
     */
    @GetMapping("/callback/google")
    public ResponseEntity<Map<String, Object>> callback(
            @RequestParam("code") String code,
            @RequestParam("state") String state) {

        Long userId = Long.parseLong(state);
        googleOAuthService.exchangeCodeForTokens(code, userId);

        var locations = googleBusinessService.fetchAndStoreLocations(userId);

        System.out.println("SUCCESS: OAuth callback complete — " + locations.size() + " locations connected for user " + userId);

        return ResponseEntity.ok(Map.of(
                "message", "Google Business Profile connected successfully",
                "locationsConnected", locations.size()
        ));
    }
}
