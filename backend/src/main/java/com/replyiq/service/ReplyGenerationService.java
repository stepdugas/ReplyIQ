package com.replyiq.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.replyiq.model.Location;
import com.replyiq.model.Review;
import com.replyiq.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReplyGenerationService {

    private final ReviewRepository reviewRepository;
    private final ReplyPostingService replyPostingService;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${replyiq.anthropic.api-key}")
    private String anthropicApiKey;

    @Value("${replyiq.anthropic.model}")
    private String model;

    private static final String ANTHROPIC_API_URL = "https://api.anthropic.com/v1/messages";

    /**
     * Generates an AI reply for a single review using Claude.
     */
    public String generateReply(Review review) {
        Location location = review.getLocation();
        String prompt = buildPrompt(review, location);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", anthropicApiKey);
        headers.set("anthropic-version", "2023-06-01");

        Map<String, Object> body = Map.of(
                "model", model,
                "max_tokens", 300,
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                )
        );

        try {
            String requestBody = objectMapper.writeValueAsString(body);
            ResponseEntity<String> response = restTemplate.exchange(
                    ANTHROPIC_API_URL, HttpMethod.POST,
                    new HttpEntity<>(requestBody, headers),
                    String.class
            );

            JsonNode json = objectMapper.readTree(response.getBody());
            String replyText = json.get("content").get(0).get("text").asText().trim();

            review.setReplyText(replyText);
            review.setReplyStatus("pending");
            reviewRepository.save(review);

            log.info("AI reply generated for review by {} at {}", review.getReviewerName(), location.getLocationName());

            // Auto-post if location has it enabled
            replyPostingService.handlePostGeneration(review);

            return replyText;
        } catch (Exception e) {
            review.setReplyStatus("failed");
            reviewRepository.save(review);
            throw new RuntimeException("Failed to generate reply: " + e.getMessage(), e);
        }
    }

    /**
     * Generates replies for all unanswered reviews for a user.
     */
    public int generateRepliesForUser(Long userId) {
        List<Review> needsReply = reviewRepository.findByUserIdAndStatus(userId, "needs_reply");
        int generated = 0;

        for (Review review : needsReply) {
            try {
                generateReply(review);
                generated++;
            } catch (Exception e) {
                log.error("Failed to generate reply for review {}: {}", review.getGoogleReviewId(), e.getMessage());
            }
        }

        log.info("Generated {} replies for user {}", generated, userId);
        return generated;
    }

    private String buildPrompt(Review review, Location location) {
        String tone = location.getTonePreference();
        String toneInstruction = switch (tone) {
            case "friendly" -> "Use a warm, conversational, and approachable tone. Feel free to use casual language and show genuine enthusiasm.";
            case "brief" -> "Keep the reply short and to the point — 1-2 sentences max. Be polite but concise.";
            default -> "Use a polished, professional tone. Be courteous and business-appropriate.";
        };

        String ratingInstruction = switch (review.getStarRating()) {
            case 5 -> "This is a glowing 5-star review. Express genuine gratitude and warmth. Mention something specific from their review to show you actually read it. Make them feel valued as a customer.";
            case 4 -> "This is a positive 4-star review. Thank them warmly and acknowledge what they enjoyed. If they mentioned anything that could be improved, briefly and graciously acknowledge it.";
            case 3 -> "This is a mixed 3-star review. Thank them for their honest feedback. Acknowledge both the positives and the concerns they raised. Show that you take their feedback seriously and are working to improve.";
            case 2 -> "This is a disappointed 2-star review. Respond with empathy and professionalism. Apologize for their experience without being defensive. Acknowledge their specific concerns and express a genuine desire to make things right.";
            case 1 -> "This is an unhappy 1-star review. Respond calmly and professionally. Do NOT be defensive or dismissive. Sincerely apologize for their poor experience, acknowledge their frustration, and offer to resolve the issue offline if appropriate.";
            default -> "Respond appropriately based on the sentiment of the review.";
        };

        return """
                You are writing a reply to a Google review on behalf of a local business. \
                Write ONLY the reply text — no quotes, no labels, no explanation.

                Business: %s
                Reviewer: %s
                Star Rating: %d/5
                Review: "%s"

                TONE: %s

                RATING GUIDANCE: %s

                RULES:
                - Write as the business owner, not as an AI
                - Never start with "Dear" — use the reviewer's first name naturally
                - Vary your opening — don't always start with "Thank you"
                - Reference specific details from their review so it feels personal
                - Never use phrases like "We appreciate your feedback" or "Your satisfaction is our priority" — those sound robotic
                - Do NOT mention being AI or automated in any way
                - Keep it under 100 words
                - Sound like a real human who genuinely cares about their business
                - Each reply should feel unique — imagine this is the only review you're responding to today
                """.formatted(
                location.getLocationName(),
                review.getReviewerName(),
                review.getStarRating(),
                review.getReviewText() != null ? review.getReviewText() : "(no text)",
                toneInstruction,
                ratingInstruction
        );
    }
}
