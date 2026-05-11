package com.replyiq.controller;

import com.replyiq.service.StripeService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.Subscription;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/stripe/webhook")
@RequiredArgsConstructor
public class StripeWebhookController {

    private final StripeService stripeService;

    @Value("${replyiq.stripe.webhook-secret}")
    private String webhookSecret;

    @PostMapping
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.error("Invalid Stripe webhook signature");
            return ResponseEntity.status(400).body("Invalid signature");
        }

        String type = event.getType();
        log.info("Stripe webhook: {}", type);

        switch (type) {
            case "customer.subscription.created",
                 "customer.subscription.updated",
                 "customer.subscription.deleted" -> {

                Subscription subscription = (Subscription) event.getDataObjectDeserializer()
                        .getObject().orElse(null);

                if (subscription != null) {
                    stripeService.handleSubscriptionUpdate(
                            subscription.getCustomer(),
                            subscription.getStatus(),
                            subscription.getCurrentPeriodEnd()
                    );
                }
            }
            case "customer.subscription.trial_will_end" -> {
                // Could send email reminder here via SendGrid
                log.info("Stripe: Trial ending soon");
            }
            default -> log.info("Stripe: Unhandled event type: {}", type);
        }

        return ResponseEntity.ok("OK");
    }
}
