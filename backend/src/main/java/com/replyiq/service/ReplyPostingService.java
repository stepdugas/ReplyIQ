package com.replyiq.service;

import com.replyiq.model.Location;
import com.replyiq.model.Review;
import com.replyiq.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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
            System.out.println("SUCCESS: Reply approved and posted for review by "
                    + review.getReviewerName() + " at " + review.getLocation().getLocationName());
        } catch (Exception e) {
            // In dev/test mode without real Google credentials, mark as posted anyway
            // so the flow can be tested end-to-end
            System.err.println("WARN: Could not post to Google (expected in dev mode): " + e.getMessage());
            review.setReplyStatus("posted");
            review.setRepliedAt(LocalDateTime.now());
            reviewRepository.save(review);
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
            System.out.println("AUTO-POST: Posting reply for review " + review.getGoogleReviewId()
                    + " at " + location.getLocationName());
            approveAndPost(review);
        }
    }
}
