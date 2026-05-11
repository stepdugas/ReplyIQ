package com.replyiq.service;

import com.replyiq.model.User;
import com.replyiq.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StripeServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private StripeService stripeService;

    private User createTestUser(String customerId) {
        return User.builder()
                .id(1L)
                .email("user@test.com")
                .name("Test User")
                .passwordHash("$2a$hash")
                .stripeCustomerId(customerId)
                .subscriptionStatus("trialing")
                .build();
    }

    @Test
    void handleSubscriptionUpdate_activeStatus_setsUserToActive() {
        User user = createTestUser("cus_123");
        when(userRepository.findByStripeCustomerId("cus_123")).thenReturn(Optional.of(user));

        stripeService.handleSubscriptionUpdate("cus_123", "active", null);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("active", captor.getValue().getSubscriptionStatus());
    }

    @Test
    void handleSubscriptionUpdate_canceledStatus_setsUserToCancelled() {
        User user = createTestUser("cus_456");
        when(userRepository.findByStripeCustomerId("cus_456")).thenReturn(Optional.of(user));

        stripeService.handleSubscriptionUpdate("cus_456", "canceled", null);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("cancelled", captor.getValue().getSubscriptionStatus());
    }

    @Test
    void handleSubscriptionUpdate_trialingStatus_keepsUserAsTrialing() {
        User user = createTestUser("cus_789");
        when(userRepository.findByStripeCustomerId("cus_789")).thenReturn(Optional.of(user));

        stripeService.handleSubscriptionUpdate("cus_789", "trialing", null);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("trialing", captor.getValue().getSubscriptionStatus());
    }

    @Test
    void handleSubscriptionUpdate_unknownCustomerId_doesNothing() {
        when(userRepository.findByStripeCustomerId("cus_unknown")).thenReturn(Optional.empty());

        stripeService.handleSubscriptionUpdate("cus_unknown", "active", null);

        verify(userRepository, never()).save(any());
    }

    @Test
    void handleSubscriptionUpdate_withCurrentPeriodEnd_setsTrialEndDate() {
        User user = createTestUser("cus_date");
        when(userRepository.findByStripeCustomerId("cus_date")).thenReturn(Optional.of(user));

        long epochSeconds = 1720000000L; // some future timestamp
        stripeService.handleSubscriptionUpdate("cus_date", "active", epochSeconds);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertNotNull(captor.getValue().getTrialEndDate());
    }

    @Test
    void handleSubscriptionUpdate_unpaidStatus_mapsToCancelled() {
        User user = createTestUser("cus_unpaid");
        when(userRepository.findByStripeCustomerId("cus_unpaid")).thenReturn(Optional.of(user));

        stripeService.handleSubscriptionUpdate("cus_unpaid", "unpaid", null);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("cancelled", captor.getValue().getSubscriptionStatus());
    }

    @Test
    void handleSubscriptionUpdate_pastDueStatus_mapsToPastDue() {
        User user = createTestUser("cus_past");
        when(userRepository.findByStripeCustomerId("cus_past")).thenReturn(Optional.of(user));

        stripeService.handleSubscriptionUpdate("cus_past", "past_due", null);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("past_due", captor.getValue().getSubscriptionStatus());
    }

    @Test
    void mapStripeStatus_mapsAllStatusesCorrectly() throws Exception {
        // Use reflection to test the private mapStripeStatus method
        Method method = StripeService.class.getDeclaredMethod("mapStripeStatus", String.class);
        method.setAccessible(true);

        assertEquals("active", method.invoke(stripeService, "active"));
        assertEquals("trialing", method.invoke(stripeService, "trialing"));
        assertEquals("past_due", method.invoke(stripeService, "past_due"));
        assertEquals("cancelled", method.invoke(stripeService, "canceled"));
        assertEquals("cancelled", method.invoke(stripeService, "unpaid"));
        // Unknown statuses pass through unchanged
        assertEquals("some_other_status", method.invoke(stripeService, "some_other_status"));
    }
}
