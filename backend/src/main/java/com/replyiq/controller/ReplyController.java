package com.replyiq.controller;

import com.replyiq.dto.ReviewResponse;
import com.replyiq.model.Review;
import com.replyiq.repository.ReviewRepository;
import com.replyiq.service.ReplyGenerationService;
import com.replyiq.service.ReplyPostingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/replies")
@RequiredArgsConstructor
public class ReplyController {

    private final ReplyGenerationService replyGenerationService;
    private final ReplyPostingService replyPostingService;
    private final ReviewRepository reviewRepository;

    /**
     * Generate an AI reply for a single review.
     */
    @PostMapping("/generate/{reviewId}")
    public ResponseEntity<Map<String, String>> generateReply(
            @PathVariable Long reviewId,
            Authentication auth) {

        Long userId = (Long) auth.getPrincipal();
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        if (!review.getLocation().getUser().getId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }

        String replyText = replyGenerationService.generateReply(review);
        return ResponseEntity.ok(Map.of("replyText", replyText));
    }

    /**
     * Generate AI replies for all unanswered reviews.
     */
    @PostMapping("/generate-all")
    public ResponseEntity<Map<String, Object>> generateAll(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        int count = replyGenerationService.generateRepliesForUser(userId);
        return ResponseEntity.ok(Map.of(
                "message", "Replies generated",
                "count", count
        ));
    }

    /**
     * Edit a pending reply before approving.
     */
    @PutMapping("/{reviewId}")
    public ResponseEntity<Map<String, String>> editReply(
            @PathVariable Long reviewId,
            @RequestBody Map<String, String> body,
            Authentication auth) {

        Long userId = (Long) auth.getPrincipal();
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        if (!review.getLocation().getUser().getId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }

        String newText = body.get("replyText");
        if (newText == null || newText.isBlank()) {
            throw new IllegalArgumentException("Reply text cannot be empty");
        }

        review.setReplyText(newText);
        reviewRepository.save(review);

        return ResponseEntity.ok(Map.of("replyText", review.getReplyText()));
    }

    /**
     * Approve a pending reply and post it to Google.
     */
    @PostMapping("/approve/{reviewId}")
    public ResponseEntity<ReviewResponse> approveReply(
            @PathVariable Long reviewId,
            Authentication auth) {

        Long userId = (Long) auth.getPrincipal();
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        if (!review.getLocation().getUser().getId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }

        if (!"pending".equals(review.getReplyStatus())) {
            throw new IllegalArgumentException("Only pending replies can be approved");
        }

        review = replyPostingService.approveAndPost(review);
        return ResponseEntity.ok(ReviewResponse.from(review));
    }

    /**
     * Regenerate the AI reply for a review (discard current and generate fresh).
     */
    @PostMapping("/regenerate/{reviewId}")
    public ResponseEntity<Map<String, String>> regenerateReply(
            @PathVariable Long reviewId,
            Authentication auth) {

        Long userId = (Long) auth.getPrincipal();
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        if (!review.getLocation().getUser().getId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }

        review.setReplyStatus("needs_reply");
        review.setReplyText(null);
        reviewRepository.save(review);

        String replyText = replyGenerationService.generateReply(review);
        return ResponseEntity.ok(Map.of("replyText", replyText));
    }
}
