package com.replyiq.service;

import com.replyiq.model.NurtureEmail;
import com.replyiq.model.User;
import com.replyiq.repository.NurtureEmailRepository;
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
public class NurtureSequenceService {

    private final UserRepository userRepository;
    private final NurtureEmailRepository nurtureEmailRepository;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;

    @Value("${replyiq.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    // Day -> email mapping: 0, 1, 2, 3, 5, 6, 7
    private static final int[] NURTURE_DAYS = {0, 1, 2, 3, 5, 6, 7};

    /**
     * Send welcome email immediately when a user signs up.
     * Called from AuthService after signup.
     */
    public void sendWelcomeEmail(User user) {
        sendNurtureEmail(user, 0);
    }

    /**
     * Runs every hour, checks all trialing users and sends
     * the appropriate nurture email based on their trial age.
     */
    @Scheduled(cron = "0 0 * * * *")
    public void processNurtureSequence() {
        log.info("NURTURE: Processing nurture sequence...");

        List<User> trialingUsers = userRepository.findBySubscriptionStatusIn(List.of("trialing"));
        int sent = 0;

        for (User user : trialingUsers) {
            if (!user.getEmailNotifications()) continue;
            if (user.getCreatedAt() == null) continue;

            long daysSinceSignup = ChronoUnit.DAYS.between(user.getCreatedAt(), LocalDateTime.now());

            for (int day : NURTURE_DAYS) {
                if (daysSinceSignup >= day && !nurtureEmailRepository.existsByUserIdAndEmailDay(user.getId(), day)) {
                    if (sendNurtureEmail(user, day)) sent++;
                }
            }
        }

        log.info("NURTURE: Complete — sent {} emails", sent);
    }

    private boolean sendNurtureEmail(User user, int day) {
        // Don't send if already paid
        if ("active".equals(user.getSubscriptionStatus())) return false;
        // Don't send if already sent this day's email
        if (nurtureEmailRepository.existsByUserIdAndEmailDay(user.getId(), day)) return false;

        String subject = getSubject(day);
        String body = getBody(user, day);

        try {
            emailService.sendEmail(user.getEmail(), subject, body);
            nurtureEmailRepository.save(NurtureEmail.builder()
                    .userId(user.getId())
                    .emailDay(day)
                    .build());
            log.info("NURTURE: Sent day {} email to {}", day, user.getEmail());
            return true;
        } catch (Exception e) {
            log.error("NURTURE: Failed to send day {} email to {}: {}", day, user.getEmail(), e.getMessage());
            return false;
        }
    }

    private String getSubject(int day) {
        return switch (day) {
            case 0 -> "Welcome to ReplyIQ — here's how to get started";
            case 1 -> "Did you know unanswered reviews hurt your ranking?";
            case 2 -> "Your first AI reply is ready to post";
            case 3 -> "How local business owners use ReplyIQ";
            case 5 -> "You have 2 days left in your trial";
            case 6 -> "Last chance — your trial ends tomorrow";
            case 7 -> "Your ReplyIQ trial has ended";
            default -> "ReplyIQ update";
        };
    }

    private String getBody(User user, int day) {
        String unsubUrl = frontendUrl + "/api/notifications/unsubscribe?token="
                + jwtUtil.generateToken(user.getId(), user.getEmail(), "unsubscribe");
        String dashUrl = frontendUrl + "/dashboard";
        String name = user.getName().split(" ")[0]; // First name

        String content = switch (day) {
            case 0 -> day0Content(name, dashUrl);
            case 1 -> day1Content(name, dashUrl);
            case 2 -> day2Content(name, dashUrl);
            case 3 -> day3Content(name);
            case 5 -> day5Content(name, dashUrl);
            case 6 -> day6Content(name, dashUrl);
            case 7 -> day7Content(name, dashUrl);
            default -> "";
        };

        return emailWrapper(content, unsubUrl);
    }

    private String day0Content(String name, String dashUrl) {
        return """
            <h2 style="color:#ffffff;font-size:20px;margin-bottom:12px;">Welcome to ReplyIQ, %s!</h2>
            <p style="color:#d1d5db;font-size:14px;line-height:1.7;margin-bottom:20px;">
              You just took the first step toward never worrying about Google reviews again. Here's how to get set up in under 2 minutes:
            </p>
            <div style="background:#1a1f2e;border:1px solid #2a3040;border-radius:10px;padding:20px;margin-bottom:20px;">
              <div style="color:#d1d5db;font-size:14px;line-height:2;">
                <strong style="color:#10b981;">Step 1:</strong> Connect your Google Business Profile<br>
                <strong style="color:#10b981;">Step 2:</strong> Choose your tone (professional, friendly, or brief)<br>
                <strong style="color:#10b981;">Step 3:</strong> Pick auto-post or approve-first mode
              </div>
            </div>
            <p style="color:#9ca3af;font-size:14px;line-height:1.6;margin-bottom:24px;">
              That's it. Once connected, ReplyIQ monitors your reviews every 30 minutes and generates natural, human-sounding replies automatically.
            </p>
            <div style="text-align:center;">
              <a href="%s" style="display:inline-block;background:#10b981;color:white;padding:12px 28px;border-radius:8px;text-decoration:none;font-weight:600;font-size:14px;">Connect Your Business</a>
            </div>
            """.formatted(name, dashUrl);
    }

    private String day1Content(String name, String dashUrl) {
        return """
            <h2 style="color:#ffffff;font-size:20px;margin-bottom:12px;">Hey %s — did you know this?</h2>
            <div style="background:#1a1f2e;border:1px solid #2a3040;border-radius:10px;padding:24px;margin-bottom:20px;text-align:center;">
              <div style="color:#10b981;font-size:36px;font-weight:700;">88%%</div>
              <div style="color:#d1d5db;font-size:14px;margin-top:8px;">of consumers read review responses before choosing a business</div>
            </div>
            <p style="color:#d1d5db;font-size:14px;line-height:1.7;margin-bottom:16px;">
              Google also uses review response rate as a ranking factor. Businesses that respond to reviews consistently show up higher in local search results and Google Maps.
            </p>
            <p style="color:#d1d5db;font-size:14px;line-height:1.7;margin-bottom:24px;">
              The problem? Most owners start responding and then fall off after a week. ReplyIQ makes sure every review gets a response — automatically.
            </p>
            <div style="text-align:center;">
              <a href="%s" style="display:inline-block;background:#10b981;color:white;padding:12px 28px;border-radius:8px;text-decoration:none;font-weight:600;font-size:14px;">Go to Dashboard</a>
            </div>
            """.formatted(name, dashUrl);
    }

    private String day2Content(String name, String dashUrl) {
        return """
            <h2 style="color:#ffffff;font-size:20px;margin-bottom:12px;">Your AI replies are waiting, %s</h2>
            <p style="color:#d1d5db;font-size:14px;line-height:1.7;margin-bottom:16px;">
              ReplyIQ has been monitoring your reviews and generating replies. Head to your dashboard to see them.
            </p>
            <p style="color:#d1d5db;font-size:14px;line-height:1.7;margin-bottom:16px;">
              Quick tip — you have two modes:
            </p>
            <div style="background:#1a1f2e;border:1px solid #2a3040;border-radius:10px;padding:20px;margin-bottom:20px;">
              <div style="color:#d1d5db;font-size:14px;line-height:2;">
                <strong style="color:#10b981;">Auto-post:</strong> Replies go live immediately. Fully hands-free.<br>
                <strong style="color:#10b981;">Approve first:</strong> Review and edit each reply before it posts. Full control.
              </div>
            </div>
            <p style="color:#9ca3af;font-size:14px;line-height:1.6;margin-bottom:24px;">
              You can switch between modes anytime in your location settings. Most owners start with approve-first, then switch to auto-post once they trust the AI.
            </p>
            <div style="text-align:center;">
              <a href="%s" style="display:inline-block;background:#10b981;color:white;padding:12px 28px;border-radius:8px;text-decoration:none;font-weight:600;font-size:14px;">Review Your Replies</a>
            </div>
            """.formatted(name, dashUrl);
    }

    private String day3Content(String name) {
        return """
            <h2 style="color:#ffffff;font-size:20px;margin-bottom:12px;">How owners like you use ReplyIQ</h2>
            <p style="color:#d1d5db;font-size:14px;line-height:1.7;margin-bottom:20px;">
              Hey %s — wanted to share a quick story.
            </p>
            <div style="background:#1a1f2e;border:1px solid #2a3040;border-radius:10px;padding:20px;margin-bottom:20px;">
              <p style="color:#d1d5db;font-size:14px;line-height:1.7;margin:0;">
                <em>"I run a dental practice with two locations. Before ReplyIQ, I had 40+ unanswered reviews across both offices. I kept meaning to respond but never had time between patients. I set up ReplyIQ on a Tuesday, and by Wednesday morning every review had a professional, personalized response. My office manager thought I stayed up all night writing them."</em>
              </p>
              <p style="color:#9ca3af;font-size:13px;margin-top:12px;margin-bottom:0;">
                — Small business owner, 2 locations
              </p>
            </div>
            <p style="color:#d1d5db;font-size:14px;line-height:1.7;margin-bottom:0;">
              The best part? It takes 2 minutes to set up and runs on autopilot. Your reviews are too important to ignore — and now you don't have to.
            </p>
            """.formatted(name);
    }

    private String day5Content(String name, String dashUrl) {
        return """
            <h2 style="color:#ffffff;font-size:20px;margin-bottom:12px;">%s, your trial ends in 2 days</h2>
            <p style="color:#d1d5db;font-size:14px;line-height:1.7;margin-bottom:20px;">
              Just a heads up — your free trial wraps up in 2 days. If ReplyIQ has been helpful, here's what you keep with a subscription:
            </p>
            <div style="background:#1a1f2e;border:1px solid #2a3040;border-radius:10px;padding:20px;margin-bottom:20px;">
              <div style="color:#d1d5db;font-size:14px;line-height:2;">
                &#10003; Unlimited review monitoring (every 30 minutes)<br>
                &#10003; AI-generated replies for every review<br>
                &#10003; Auto-post or approve-first mode<br>
                &#10003; Multiple locations supported<br>
                &#10003; Dashboard with analytics
              </div>
              <div style="color:#10b981;font-size:20px;font-weight:700;margin-top:16px;">$19.99/month — cancel anytime</div>
            </div>
            <p style="color:#9ca3af;font-size:14px;line-height:1.6;margin-bottom:24px;">
              No pressure at all. If it's not for you, your data stays safe and you can come back anytime.
            </p>
            <div style="text-align:center;">
              <a href="%s" style="display:inline-block;background:#10b981;color:white;padding:14px 32px;border-radius:8px;text-decoration:none;font-weight:600;font-size:15px;">Subscribe Now</a>
            </div>
            """.formatted(name, dashUrl);
    }

    private String day6Content(String name, String dashUrl) {
        return """
            <h2 style="color:#ffffff;font-size:20px;margin-bottom:12px;">Last day, %s</h2>
            <p style="color:#d1d5db;font-size:14px;line-height:1.7;margin-bottom:24px;">
              Your ReplyIQ trial ends tomorrow. After that, your review monitoring and AI replies will pause.
            </p>
            <p style="color:#ffffff;font-size:16px;font-weight:600;margin-bottom:24px;text-align:center;">
              Keep every Google review responded to — automatically — for $19.99/month.
            </p>
            <div style="text-align:center;">
              <a href="%s" style="display:inline-block;background:#10b981;color:white;padding:14px 36px;border-radius:8px;text-decoration:none;font-weight:700;font-size:16px;">Subscribe Now</a>
            </div>
            """.formatted(name, dashUrl);
    }

    private String day7Content(String name, String dashUrl) {
        return """
            <h2 style="color:#ffffff;font-size:20px;margin-bottom:12px;">Your trial has ended</h2>
            <p style="color:#d1d5db;font-size:14px;line-height:1.7;margin-bottom:16px;">
              Hey %s — your 7-day ReplyIQ trial is over. Your review monitoring and AI replies have been paused, but your data is still here.
            </p>
            <p style="color:#d1d5db;font-size:14px;line-height:1.7;margin-bottom:24px;">
              If you'd like to pick back up, you can reactivate anytime. Your locations, settings, and review history are all preserved.
            </p>
            <div style="text-align:center;margin-bottom:24px;">
              <a href="%s" style="display:inline-block;background:#10b981;color:white;padding:12px 28px;border-radius:8px;text-decoration:none;font-weight:600;font-size:14px;">Reactivate My Account</a>
            </div>
            <p style="color:#9ca3af;font-size:13px;line-height:1.6;">
              Questions? Just reply to this email — I read every one.<br>
              — Stephanie
            </p>
            """.formatted(name, dashUrl);
    }

    private String emailWrapper(String content, String unsubUrl) {
        return """
            <div style="font-family:'Helvetica Neue',Arial,sans-serif;max-width:520px;margin:0 auto;background:#0f1320;padding:0;">
              <div style="padding:32px 24px;">
                <div style="text-align:center;margin-bottom:28px;">
                  <div style="display:inline-block;background:linear-gradient(135deg,#10b981,#14b8a6);width:36px;height:36px;border-radius:9px;line-height:36px;color:white;font-weight:bold;font-size:16px;">R</div>
                  <span style="font-size:18px;font-weight:bold;color:#ffffff;margin-left:8px;vertical-align:middle;">ReplyIQ</span>
                </div>
                %s
                <hr style="border:none;border-top:1px solid #2a3040;margin:28px 0 16px;">
                <p style="color:#6b7280;font-size:11px;text-align:center;line-height:1.5;">
                  &copy; 2026 Erie Apps LLC &middot;
                  <a href="%s" style="color:#6b7280;text-decoration:underline;">Unsubscribe</a>
                </p>
              </div>
            </div>
            """.formatted(content, unsubUrl);
    }
}
