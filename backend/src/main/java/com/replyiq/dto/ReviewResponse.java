package com.replyiq.dto;

import com.replyiq.model.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class ReviewResponse {
    private Long id;
    private Long locationId;
    private String locationName;
    private String googleReviewId;
    private String reviewerName;
    private Integer starRating;
    private String reviewText;
    private LocalDateTime postedAt;
    private String replyText;
    private String replyStatus;
    private LocalDateTime repliedAt;

    public static ReviewResponse from(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .locationId(review.getLocation().getId())
                .locationName(review.getLocation().getLocationName())
                .googleReviewId(review.getGoogleReviewId())
                .reviewerName(review.getReviewerName())
                .starRating(review.getStarRating())
                .reviewText(review.getReviewText())
                .postedAt(review.getPostedAt())
                .replyText(review.getReplyText())
                .replyStatus(review.getReplyStatus())
                .repliedAt(review.getRepliedAt())
                .build();
    }
}
