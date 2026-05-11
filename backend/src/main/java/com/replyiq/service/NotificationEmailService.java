package com.replyiq.service;

import com.replyiq.model.Review;
import com.replyiq.model.User;
import com.replyiq.repository.ReviewRepository;
import com.replyiq.repository.UserRepository;
import com.replyiq.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationEmailService {

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;

    @Value("${replyiq.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    /**
     * Daily at 8am — notify users with unanswered reviews.
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void sendDailyUnansweredAlerts() {
        log.info("Running daily unanswered review alerts...");

        List<User> users = getNotifiableUsers();
        int sent = 0;

        for (User user : users) {
            List<Review> unanswered = reviewRepository.findByUserIdAndStatus(user.getId(), "needs_reply");
            if (unanswered.isEmpty()) continue;

            String unsubUrl = buildUnsubscribeUrl(user);
            String subject = "You have " + unanswered.size() + " unanswered Google review"
                    + (unanswered.size() > 1 ? "s" : "") + " — ReplyIQ";
            String html = buildDailyAlertHtml(user, unanswered, unsubUrl);

            try {
                emailService.sendEmail(user.getEmail(), subject, html);
                sent++;
            } catch (Exception e) {
                log.error("Failed to send daily alert to {}: {}", user.getEmail(), e.getMessage());
            }
        }

        log.info("Daily alerts complete — sent {} emails", sent);
    }

    /**
     * Every Monday at 8am — weekly summary.
     */
    @Scheduled(cron = "0 0 8 * * MON")
    public void sendWeeklySummaries() {
        log.info("Running weekly summaries...");

        List<User> users = getNotifiableUsers();
        LocalDateTime weekAgo = LocalDateTime.now().minus(7, ChronoUnit.DAYS);
        int sent = 0;

        for (User user : users) {
            long repliedThisWeek = reviewRepository.countRepliedSince(user.getId(), weekAgo);
            Double avgRating = reviewRepository.averageRatingByUserId(user.getId());
            long totalReviews = reviewRepository.countByUserId(user.getId());

            String unsubUrl = buildUnsubscribeUrl(user);
            String subject = "Your ReplyIQ weekly summary";
            String html = buildWeeklySummaryHtml(user, repliedThisWeek, avgRating, totalReviews, unsubUrl);

            try {
                emailService.sendEmail(user.getEmail(), subject, html);
                sent++;
            } catch (Exception e) {
                log.error("Failed to send weekly summary to {}: {}", user.getEmail(), e.getMessage());
            }
        }

        log.info("Weekly summaries complete — sent {} emails", sent);
    }

    /**
     * Daily at 8am — check for trials ending in 2 days.
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void sendTrialEndingReminders() {
        log.info("Checking for expiring trials...");

        LocalDateTime twoDaysFromNow = LocalDateTime.now().plus(2, ChronoUnit.DAYS);
        LocalDateTime threeDaysFromNow = LocalDateTime.now().plus(3, ChronoUnit.DAYS);
        int sent = 0;

        List<User> trialingUsers = userRepository.findBySubscriptionStatusIn(List.of("trialing")).stream()
                .filter(u -> u.getEmailNotifications())
                .filter(u -> u.getTrialEndDate() != null
                        && u.getTrialEndDate().isAfter(twoDaysFromNow)
                        && u.getTrialEndDate().isBefore(threeDaysFromNow))
                .toList();

        for (User user : trialingUsers) {
            String unsubUrl = buildUnsubscribeUrl(user);
            String subject = "Your ReplyIQ trial ends in 2 days";
            String html = buildTrialEndingHtml(user, unsubUrl);

            try {
                emailService.sendEmail(user.getEmail(), subject, html);
                sent++;
            } catch (Exception e) {
                log.error("Failed to send trial reminder to {}: {}", user.getEmail(), e.getMessage());
            }
        }

        log.info("Trial reminders complete — sent {} emails", sent);
    }

    private List<User> getNotifiableUsers() {
        return userRepository.findBySubscriptionStatusIn(List.of("active", "trialing")).stream()
                .filter(u -> u.getEmailNotifications())
                .toList();
    }

    private String buildUnsubscribeUrl(User user) {
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), "unsubscribe");
        return frontendUrl + "/api/notifications/unsubscribe?token=" + token;
    }

    private String emailWrapper(String content, String unsubUrl) {
        return """
                <div style="font-family: 'Helvetica Neue', Arial, sans-serif; max-width: 520px; margin: 0 auto; background: #0f1320; padding: 0;">
                  <div style="padding: 32px 24px;">
                    <div style="text-align: center; margin-bottom: 28px;">
                      <div style="display: inline-block; background: linear-gradient(135deg, #10b981, #14b8a6); width: 36px; height: 36px; border-radius: 9px; line-height: 36px; color: white; font-weight: bold; font-size: 16px;">R</div>
                      <span style="font-size: 18px; font-weight: bold; color: #ffffff; margin-left: 8px; vertical-align: middle;">ReplyIQ</span>
                    </div>
                    %s
                    <hr style="border: none; border-top: 1px solid #2a3040; margin: 28px 0 16px;">
                    <p style="color: #6b7280; font-size: 11px; text-align: center; line-height: 1.5;">
                      &copy; 2026 Erie Apps LLC &middot;
                      <a href="%s" style="color: #6b7280; text-decoration: underline;">Unsubscribe</a>
                    </p>
                  </div>
                </div>
                """.formatted(content, unsubUrl);
    }

    private String buildDailyAlertHtml(User user, List<Review> unanswered, String unsubUrl) {
        StringBuilder reviews = new StringBuilder();
        int shown = Math.min(unanswered.size(), 5);
        for (int i = 0; i < shown; i++) {
            Review r = unanswered.get(i);
            String stars = "\u2B50".repeat(r.getStarRating());
            String snippet = r.getReviewText() != null && r.getReviewText().length() > 80
                    ? r.getReviewText().substring(0, 80) + "..."
                    : (r.getReviewText() != null ? r.getReviewText() : "(no text)");
            reviews.append("""
                    <div style="background: #1a1f2e; border: 1px solid #2a3040; border-radius: 10px; padding: 14px; margin-bottom: 10px;">
                      <div style="color: #ffffff; font-size: 13px; font-weight: 600;">%s</div>
                      <div style="font-size: 12px; margin: 4px 0;">%s</div>
                      <div style="color: #9ca3af; font-size: 12px; line-height: 1.4;">"%s"</div>
                    </div>
                    """.formatted(r.getReviewerName(), stars, snippet));
        }
        if (unanswered.size() > 5) {
            reviews.append("<p style=\"color: #9ca3af; font-size: 12px; text-align: center;\">+ ")
                    .append(unanswered.size() - 5)
                    .append(" more reviews</p>");
        }

        String content = """
                <h2 style="color: #ffffff; font-size: 20px; margin-bottom: 8px; text-align: center;">You have %d unanswered review%s</h2>
                <p style="color: #9ca3af; font-size: 13px; margin-bottom: 20px; text-align: center;">
                  Here's what your customers are saying — reply in one click.
                </p>
                %s
                <div style="text-align: center; margin-top: 20px;">
                  <a href="%s/dashboard" style="display: inline-block; background: #10b981; color: white; padding: 12px 28px; border-radius: 8px; text-decoration: none; font-weight: 600; font-size: 14px;">Reply Now</a>
                </div>
                """.formatted(unanswered.size(), unanswered.size() > 1 ? "s" : "", reviews.toString(), frontendUrl);

        return emailWrapper(content, unsubUrl);
    }

    private String buildWeeklySummaryHtml(User user, long repliedThisWeek, Double avgRating, long totalReviews, String unsubUrl) {
        String ratingDisplay = avgRating != null ? String.format("%.1f", avgRating) : "—";

        String content = """
                <h2 style="color: #ffffff; font-size: 20px; margin-bottom: 8px; text-align: center;">Your weekly summary</h2>
                <p style="color: #9ca3af; font-size: 13px; margin-bottom: 24px; text-align: center;">Here's how your reviews looked this week.</p>
                <div style="display: flex; gap: 12px; margin-bottom: 20px;">
                  <div style="flex: 1; background: #1a1f2e; border: 1px solid #2a3040; border-radius: 10px; padding: 16px; text-align: center;">
                    <div style="color: #10b981; font-size: 28px; font-weight: bold;">%d</div>
                    <div style="color: #9ca3af; font-size: 11px; margin-top: 4px;">Replies sent</div>
                  </div>
                  <div style="flex: 1; background: #1a1f2e; border: 1px solid #2a3040; border-radius: 10px; padding: 16px; text-align: center;">
                    <div style="color: #10b981; font-size: 28px; font-weight: bold;">%s</div>
                    <div style="color: #9ca3af; font-size: 11px; margin-top: 4px;">Avg rating</div>
                  </div>
                  <div style="flex: 1; background: #1a1f2e; border: 1px solid #2a3040; border-radius: 10px; padding: 16px; text-align: center;">
                    <div style="color: #10b981; font-size: 28px; font-weight: bold;">%d</div>
                    <div style="color: #9ca3af; font-size: 11px; margin-top: 4px;">Total reviews</div>
                  </div>
                </div>
                <div style="text-align: center;">
                  <a href="%s/dashboard" style="display: inline-block; background: #10b981; color: white; padding: 12px 28px; border-radius: 8px; text-decoration: none; font-weight: 600; font-size: 14px;">View Dashboard</a>
                </div>
                """.formatted(repliedThisWeek, ratingDisplay, totalReviews, frontendUrl);

        return emailWrapper(content, unsubUrl);
    }

    private String buildTrialEndingHtml(User user, String unsubUrl) {
        String content = """
                <h2 style="color: #ffffff; font-size: 20px; margin-bottom: 8px; text-align: center;">Your trial ends in 2 days</h2>
                <p style="color: #9ca3af; font-size: 13px; margin-bottom: 24px; text-align: center; line-height: 1.5;">
                  Your ReplyIQ free trial is almost over. Subscribe now to keep your review monitoring and AI replies running.
                </p>
                <div style="background: #1a1f2e; border: 1px solid #2a3040; border-radius: 10px; padding: 20px; margin-bottom: 20px;">
                  <div style="color: #ffffff; font-size: 14px; font-weight: 600; margin-bottom: 12px;">What you'll keep with ReplyIQ Pro:</div>
                  <div style="color: #d1d5db; font-size: 13px; line-height: 2;">
                    &#10003; Unlimited review monitoring<br>
                    &#10003; AI-generated replies for every review<br>
                    &#10003; Auto-post or approve-first mode<br>
                    &#10003; Multiple locations supported<br>
                    &#10003; Dashboard with analytics
                  </div>
                  <div style="color: #10b981; font-size: 18px; font-weight: bold; margin-top: 12px;">$19.99/month</div>
                </div>
                <div style="text-align: center;">
                  <a href="%s/dashboard" style="display: inline-block; background: #10b981; color: white; padding: 12px 28px; border-radius: 8px; text-decoration: none; font-weight: 600; font-size: 14px;">Subscribe Now</a>
                </div>
                """.formatted(frontendUrl);

        return emailWrapper(content, unsubUrl);
    }
}
