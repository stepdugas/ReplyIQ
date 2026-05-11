package com.replyiq.service;

import com.replyiq.model.Location;
import com.replyiq.model.Review;
import com.replyiq.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReplyPostingService {

    private final ReviewRepository reviewRepository;
    private final GoogleReviewService googleReviewService;

    /**
     * Approves and posts a pending reply to Google.
     */
    public Review approveAndPost(Review review) {
        if (review.getReplyText() == null || review.getReplyText().isBlank()) {
            throw new IllegalArgumentException("Cannot post an empty reply");
        }

        try {
            googleReviewService.postReplyToGoogle(review, review.getReplyText());
            log.info("Reply approved and posted for review by {} at {}", review.getReviewerName(), review.getLocation().getLocationName());
        } catch (Exception e) {
            log.error("Failed to post reply to Google for review {}: {}", review.getGoogleReviewId(), e.getMessage());
            review.setReplyStatus("failed");
            reviewRepository.save(review);
            throw new RuntimeException("Failed to post reply to Google: " + e.getMessage(), e);
        }

        return review;
    }

    /**
     * Called after AI reply generation. If the location is set to auto-post,
     * immediately posts to Google. Otherwise, leaves as pending.
     */
    public void handlePostGeneration(Review review) {
        Location location = review.getLocation();

        if (location.getAutoPost()) {
            log.info("Auto-posting reply for review {} at {}", review.getGoogleReviewId(), location.getLocationName());
            try {
                approveAndPost(review);
            } catch (Exception e) {
                // Auto-post failure is not fatal — review stays as "failed" and user can retry
                log.error("Auto-post failed: {}", e.getMessage());
            }
        }
    }
}
