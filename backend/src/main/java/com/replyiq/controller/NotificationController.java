package com.replyiq.controller;

import com.replyiq.model.User;
import com.replyiq.repository.UserRepository;
import com.replyiq.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Value("${replyiq.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @GetMapping("/unsubscribe")
    public ResponseEntity<String> unsubscribe(@RequestParam("token") String token) {
        if (!jwtUtil.isTokenValid(token)) {
            return ResponseEntity.badRequest().body("Invalid or expired unsubscribe link.");
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        userRepository.findById(userId).ifPresent(user -> {
            user.setEmailNotifications(false);
            userRepository.save(user);
            System.out.println("SUCCESS: User " + user.getEmail() + " unsubscribed from notifications");
        });

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(frontendUrl + "/login?unsubscribed=true"));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @PostMapping("/preferences")
    public ResponseEntity<Map<String, Object>> updatePreferences(
            @RequestBody Map<String, Object> body,
            Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (body.containsKey("emailNotifications")) {
            user.setEmailNotifications((Boolean) body.get("emailNotifications"));
            userRepository.save(user);
        }

        return ResponseEntity.ok(Map.of("emailNotifications", user.getEmailNotifications()));
    }
}
