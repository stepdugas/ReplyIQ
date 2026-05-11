package com.replyiq.service;

import com.replyiq.model.Location;
import com.replyiq.model.User;
import com.replyiq.repository.LocationRepository;
import com.replyiq.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
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
        log.info("Starting review poll cycle...");

        List<User> activeUsers = userRepository.findBySubscriptionStatusIn(List.of("active", "trialing"));

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
                        log.info("Found {} new reviews for '{}' (user: {})", newCount, location.getLocationName(), user.getEmail());
                    }
                } catch (Exception e) {
                    log.error("Failed to poll reviews for '{}': {}", location.getLocationName(), e.getMessage());
                }
            }
        }

        log.info("Poll complete — checked {} locations, found {} new reviews", totalLocations, totalNewReviews);
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
                log.error("Poll error for {}: {}", location.getLocationName(), e.getMessage());
            }
        }

        log.info("On-demand poll for user {} — {} new reviews", userId, totalNew);
        return totalNew;
    }
}
