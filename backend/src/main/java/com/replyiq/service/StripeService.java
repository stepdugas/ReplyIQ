package com.replyiq.service;

import com.replyiq.model.User;
import com.replyiq.repository.UserRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class StripeService {

    private final UserRepository userRepository;

    @Value("${replyiq.stripe.price-id}")
    private String priceId;

    @Value("${replyiq.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    public String getOrCreateCustomerId(User user) throws StripeException {
        if (user.getStripeCustomerId() != null) {
            return user.getStripeCustomerId();
        }

        Customer customer = Customer.create(
                CustomerCreateParams.builder()
                        .setEmail(user.getEmail())
                        .setName(user.getName())
                        .putMetadata("userId", user.getId().toString())
                        .build()
        );

        user.setStripeCustomerId(customer.getId());
        userRepository.save(user);
        log.info("Stripe customer created for {}", user.getEmail());
        return customer.getId();
    }

    /**
     * Creates a Stripe Checkout session for subscribing.
     * $19.99/month with 7-day free trial, no card required upfront.
     */
    public String createCheckoutSession(User user) throws StripeException {
        String customerId = getOrCreateCustomerId(user);

        SessionCreateParams params = SessionCreateParams.builder()
                .setCustomer(customerId)
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPrice(priceId)
                                .setQuantity(1L)
                                .build()
                )
                .setSubscriptionData(
                        SessionCreateParams.SubscriptionData.builder()
                                .setTrialPeriodDays(7L)
                                .build()
                )
                .setPaymentMethodCollection(SessionCreateParams.PaymentMethodCollection.IF_REQUIRED)
                .setSuccessUrl(frontendUrl + "/dashboard?subscription=success")
                .setCancelUrl(frontendUrl + "/dashboard?subscription=cancelled")
                .build();

        Session session = Session.create(params);
        return session.getUrl();
    }

    /**
     * Creates a Stripe billing portal session for managing subscription.
     */
    public String createPortalSession(User user) throws StripeException {
        String customerId = getOrCreateCustomerId(user);

        var params = com.stripe.param.billingportal.SessionCreateParams.builder()
                .setCustomer(customerId)
                .setReturnUrl(frontendUrl + "/dashboard")
                .build();

        var session = com.stripe.model.billingportal.Session.create(params);
        return session.getUrl();
    }

    /**
     * Handles subscription status changes from Stripe webhooks.
     */
    public void handleSubscriptionUpdate(String customerId, String status, Long currentPeriodEnd) {
        userRepository.findByStripeCustomerId(customerId)
                .ifPresent(user -> {
                    String mappedStatus = mapStripeStatus(status);
                    user.setSubscriptionStatus(mappedStatus);

                    if (currentPeriodEnd != null) {
                        user.setTrialEndDate(
                                LocalDateTime.ofInstant(
                                        Instant.ofEpochSecond(currentPeriodEnd),
                                        ZoneId.systemDefault()
                                )
                        );
                    }

                    userRepository.save(user);
                    log.info("Subscription updated for {} — status: {}", user.getEmail(), mappedStatus);
                });
    }

    private String mapStripeStatus(String stripeStatus) {
        return switch (stripeStatus) {
            case "active" -> "active";
            case "trialing" -> "trialing";
            case "past_due" -> "past_due";
            case "canceled", "unpaid" -> "cancelled";
            default -> stripeStatus;
        };
    }
}
