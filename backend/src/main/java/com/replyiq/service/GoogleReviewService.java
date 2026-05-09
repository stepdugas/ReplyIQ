package com.replyiq.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.replyiq.model.Location;
import com.replyiq.model.Review;
import com.replyiq.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoogleReviewService {

    private final ReviewRepository reviewRepository;
    private final GoogleOAuthService googleOAuthService;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String GBP_API_BASE = "https://mybusiness.googleapis.com/v4";

    /**
     * Fetches all reviews for a location from Google and stores new ones.
     * Returns the count of newly stored reviews.
     */
    public int fetchAndStoreReviews(Location location) {
        String accessToken = googleOAuthService.getValidAccessToken(location.getUser().getId());
        String locationName = location.getGoogleLocationId();

        List<Review> newReviews = new ArrayList<>();
        String pageToken = null;

        do {
            String url = GBP_API_BASE + "/" + locationName + "/reviews?pageSize=50";
            if (pageToken != null) {
                url += "&pageToken=" + pageToken;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class
            );

            try {
                JsonNode json = objectMapper.readTree(response.getBody());
                JsonNode reviews = json.get("reviews");

                if (reviews != null && reviews.isArray()) {
                    for (JsonNode reviewNode : reviews) {
                        Review review = parseAndStoreReview(reviewNode, location);
                        if (review != null) {
                            newReviews.add(review);
                        }
                    }
                }

                pageToken = json.has("nextPageToken") ? json.get("nextPageToken").asText() : null;
            } catch (Exception e) {
                System.err.println("ERROR: Failed to parse reviews for " + locationName + ": " + e.getMessage());
                break;
            }
        } while (pageToken != null);

        return newReviews.size();
    }

    private Review parseAndStoreReview(JsonNode reviewNode, Location location) {
        String googleReviewId = reviewNode.get("reviewId").asText();

        // Skip if we already have this review
        if (reviewRepository.findByGoogleReviewId(googleReviewId).isPresent()) {
            return null;
        }

        String reviewerName = "Anonymous";
        if (reviewNode.has("reviewer") && reviewNode.get("reviewer").has("displayName")) {
            reviewerName = reviewNode.get("reviewer").get("displayName").asText();
        }

        int starRating = parseStarRating(reviewNode.get("starRating").asText());

        String reviewText = "";
        if (reviewNode.has("comment")) {
            reviewText = reviewNode.get("comment").asText();
        }

        LocalDateTime postedAt = LocalDateTime.now();
        if (reviewNode.has("createTime")) {
            try {
                postedAt = ZonedDateTime.parse(reviewNode.get("createTime").asText()).toLocalDateTime();
            } catch (Exception ignored) {
            }
        }

        // Check if the business owner already replied on Google
        String replyStatus = "needs_reply";
        String existingReply = null;
        if (reviewNode.has("reviewReply")) {
            replyStatus = "posted";
            existingReply = reviewNode.get("reviewReply").get("comment").asText();
        }

        Review review = Review.builder()
                .location(location)
                .googleReviewId(googleReviewId)
                .reviewerName(reviewerName)
                .starRating(starRating)
                .reviewText(reviewText)
                .postedAt(postedAt)
                .replyText(existingReply)
                .replyStatus(replyStatus)
                .repliedAt(replyStatus.equals("posted") ? LocalDateTime.now() : null)
                .build();

        return reviewRepository.save(review);
    }

    /**
     * Posts a reply to a review on Google Business Profile.
     */
    public void postReplyToGoogle(Review review, String replyText) {
        Location location = review.getLocation();
        String accessToken = googleOAuthService.getValidAccessToken(location.getUser().getId());

        String url = GBP_API_BASE + "/" + location.getGoogleLocationId()
                + "/reviews/" + review.getGoogleReviewId() + "/reply";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String body = "{\"comment\":\"" + escapeJson(replyText) + "\"}";

        restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(body, headers), String.class);

        review.setReplyText(replyText);
        review.setReplyStatus("posted");
        review.setRepliedAt(LocalDateTime.now());
        reviewRepository.save(review);

        System.out.println("SUCCESS: Reply posted to Google for review " + review.getGoogleReviewId());
    }

    private int parseStarRating(String rating) {
        return switch (rating) {
            case "ONE" -> 1;
            case "TWO" -> 2;
            case "THREE" -> 3;
            case "FOUR" -> 4;
            case "FIVE" -> 5;
            default -> 3;
        };
    }

    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
