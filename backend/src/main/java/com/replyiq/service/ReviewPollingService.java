package com.replyiq.service;

import com.replyiq.model.Location;
import com.replyiq.model.User;
import com.replyiq.repository.LocationRepository;
import com.replyiq.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewPollingService {

    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final GoogleReviewService googleReviewService;

    /**
     * Polls all connected locations for new reviews every 30 minutes.
     * Only polls for users with active subscriptions or trials.
     */
    @Scheduled(fixedRateString = "${replyiq.review-poll.interval-minutes:30}",
               timeUnit = java.util.concurrent.TimeUnit.MINUTES,
               initialDelay = 1)
    public void pollAllLocations() {
        System.out.println("POLL: Starting review poll cycle...");

        List<User> activeUsers = userRepository.findAll().stream()
                .filter(u -> "active".equals(u.getSubscriptionStatus())
                        || "trialing".equals(u.getSubscriptionStatus()))
                .toList();

        int totalNewReviews = 0;
        int totalLocations = 0;

        for (User user : activeUsers) {
            List<Location> locations = locationRepository.findByUserId(user.getId());

            for (Location location : locations) {
                try {
                    int newCount = googleReviewService.fetchAndStoreReviews(location);
                    totalNewReviews += newCount;
                    totalLocations++;

                    if (newCount > 0) {
                        System.out.println("POLL: Found " + newCount + " new reviews for '"
                                + location.getLocationName() + "' (user: " + user.getEmail() + ")");
                    }
                } catch (Exception e) {
                    System.err.println("POLL ERROR: Failed to poll reviews for '"
                            + location.getLocationName() + "': " + e.getMessage());
                }
            }
        }

        System.out.println("POLL: Complete — checked " + totalLocations
                + " locations, found " + totalNewReviews + " new reviews");
    }

    /**
     * On-demand poll for a single user (used after initial OAuth connection).
     */
    public int pollForUser(Long userId) {
        List<Location> locations = locationRepository.findByUserId(userId);
        int totalNew = 0;

        for (Location location : locations) {
            try {
                totalNew += googleReviewService.fetchAndStoreReviews(location);
            } catch (Exception e) {
                System.err.println("POLL ERROR: " + location.getLocationName() + ": " + e.getMessage());
            }
        }

        System.out.println("SUCCESS: On-demand poll for user " + userId + " — " + totalNew + " new reviews");
        return totalNew;
    }
}
