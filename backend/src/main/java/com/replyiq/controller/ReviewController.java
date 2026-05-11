package com.replyiq.controller;

import com.replyiq.dto.ReviewResponse;
import com.replyiq.model.Review;
import com.replyiq.repository.ReviewRepository;
import com.replyiq.service.ReviewPollingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final ReviewPollingService reviewPollingService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getReviews(
            Authentication auth,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long locationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Long userId = (Long) auth.getPrincipal();
        Pageable pageable = PageRequest.of(page, size);

        Page<Review> reviewPage;
        if (status != null && !status.isEmpty()) {
            reviewPage = reviewRepository.findByUserIdAndStatus(userId, status, pageable);
        } else {
            reviewPage = reviewRepository.findAllByUserId(userId, pageable);
        }

        List<ReviewResponse> reviews = reviewPage.getContent().stream()
                .map(ReviewResponse::from)
                .toList();

        if (locationId != null) {
            reviews = reviews.stream()
                    .filter(r -> r.getLocationId().equals(locationId))
                    .toList();
        }

        return ResponseEntity.ok(Map.of(
                "reviews", reviews,
                "page", reviewPage.getNumber(),
                "size", reviewPage.getSize(),
                "totalElements", reviewPage.getTotalElements(),
                "totalPages", reviewPage.getTotalPages()
        ));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();

        long total = reviewRepository.countByUserId(userId);
        long unanswered = reviewRepository.countUnansweredByUserId(userId);
        Double avgRating = reviewRepository.averageRatingByUserId(userId);
        long repliedThisMonth = reviewRepository.countRepliedSince(userId,
                LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0));

        return ResponseEntity.ok(Map.of(
                "totalReviews", total,
                "unansweredReviews", unanswered,
                "repliedThisMonth", repliedThisMonth,
                "averageRating", avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0
        ));
    }

    @PostMapping("/poll")
    public ResponseEntity<Map<String, Object>> triggerPoll(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        int newReviews = reviewPollingService.pollForUser(userId);
        return ResponseEntity.ok(Map.of(
                "message", "Poll complete",
                "newReviews", newReviews
        ));
    }
}
