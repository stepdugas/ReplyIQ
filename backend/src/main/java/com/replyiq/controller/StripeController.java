package com.replyiq.controller;

import com.replyiq.model.User;
import com.replyiq.repository.UserRepository;
import com.replyiq.service.StripeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/stripe")
@RequiredArgsConstructor
public class StripeController {

    private final StripeService stripeService;
    private final UserRepository userRepository;

    @PostMapping("/checkout")
    public ResponseEntity<Map<String, String>> createCheckout(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        try {
            String url = stripeService.createCheckoutSession(user);
            return ResponseEntity.ok(Map.of("url", url));
        } catch (Exception e) {
            throw new RuntimeException("Failed to create checkout session: " + e.getMessage(), e);
        }
    }

    @PostMapping("/portal")
    public ResponseEntity<Map<String, String>> createPortal(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        try {
            String url = stripeService.createPortalSession(user);
            return ResponseEntity.ok(Map.of("url", url));
        } catch (Exception e) {
            throw new RuntimeException("Failed to create portal session: " + e.getMessage(), e);
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return ResponseEntity.ok(Map.of(
                "subscriptionStatus", user.getSubscriptionStatus(),
                "trialEndDate", user.getTrialEndDate() != null ? user.getTrialEndDate().toString() : "",
                "hasStripeCustomer", user.getStripeCustomerId() != null
        ));
    }
}
