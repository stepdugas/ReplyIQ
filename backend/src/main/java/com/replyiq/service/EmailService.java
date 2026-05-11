package com.replyiq.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${replyiq.sendgrid.api-key}")
    private String sendgridApiKey;

    @Value("${replyiq.sendgrid.from-email:stephanie@erie-apps.com}")
    private String fromEmail;

    @Value("${replyiq.sendgrid.from-name:ReplyIQ}")
    private String fromName;

    private static final String SENDGRID_API_URL = "https://api.sendgrid.com/v3/mail/send";

    public void sendPasswordResetEmail(String toEmail, String resetUrl) {
        String subject = "Reset your ReplyIQ password";
        String html = buildResetEmailHtml(resetUrl);
        sendEmail(toEmail, subject, html);
        log.info("Password reset email sent to {}", toEmail);
    }

    public void sendPasswordResetConfirmation(String toEmail) {
        String subject = "Your ReplyIQ password has been reset";
        String html = buildResetConfirmationHtml();
        sendEmail(toEmail, subject, html);
        log.info("Password reset confirmation sent to {}", toEmail);
    }

    public void sendEmail(String toEmail, String subject, String htmlContent) {
        if ("placeholder".equals(sendgridApiKey)) {
            log.warn("SendGrid API key not set — skipping email to {}", toEmail);
            return;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + sendgridApiKey);

        Map<String, Object> body = Map.of(
                "personalizations", List.of(Map.of(
                        "to", List.of(Map.of("email", toEmail))
                )),
                "from", Map.of("email", fromEmail, "name", fromName),
                "subject", subject,
                "content", List.of(Map.of("type", "text/html", "value", htmlContent))
        );

        try {
            String requestBody = objectMapper.writeValueAsString(body);
            restTemplate.exchange(SENDGRID_API_URL, HttpMethod.POST,
                    new HttpEntity<>(requestBody, headers), String.class);
        } catch (Exception e) {
            log.error("Failed to send email via SendGrid: {}", e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String buildResetEmailHtml(String resetUrl) {
        return """
                <div style="font-family: 'Helvetica Neue', Arial, sans-serif; max-width: 480px; margin: 0 auto; padding: 40px 20px;">
                  <div style="text-align: center; margin-bottom: 32px;">
                    <div style="display: inline-block; background: linear-gradient(135deg, #10b981, #14b8a6); width: 40px; height: 40px; border-radius: 10px; line-height: 40px; color: white; font-weight: bold; font-size: 18px;">R</div>
                    <span style="font-size: 20px; font-weight: bold; color: #1a1a2e; margin-left: 8px; vertical-align: middle;">ReplyIQ</span>
                  </div>
                  <h2 style="color: #1a1a2e; font-size: 22px; margin-bottom: 16px; text-align: center;">Reset your password</h2>
                  <p style="color: #6b7280; font-size: 14px; line-height: 1.6; margin-bottom: 24px; text-align: center;">
                    We received a request to reset your password. Click the button below to choose a new one. This link expires in 1 hour.
                  </p>
                  <div style="text-align: center; margin-bottom: 24px;">
                    <a href="%s" style="display: inline-block; background: #10b981; color: white; padding: 12px 32px; border-radius: 8px; text-decoration: none; font-weight: 600; font-size: 14px;">Reset Password</a>
                  </div>
                  <p style="color: #9ca3af; font-size: 12px; line-height: 1.5; text-align: center;">
                    If you didn't request this, you can safely ignore this email. Your password won't change.
                  </p>
                  <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 32px 0 16px;">
                  <p style="color: #d1d5db; font-size: 11px; text-align: center;">&copy; 2026 Erie Apps LLC</p>
                </div>
                """.formatted(resetUrl);
    }

    private String buildResetConfirmationHtml() {
        return """
                <div style="font-family: 'Helvetica Neue', Arial, sans-serif; max-width: 480px; margin: 0 auto; padding: 40px 20px;">
                  <div style="text-align: center; margin-bottom: 32px;">
                    <div style="display: inline-block; background: linear-gradient(135deg, #10b981, #14b8a6); width: 40px; height: 40px; border-radius: 10px; line-height: 40px; color: white; font-weight: bold; font-size: 18px;">R</div>
                    <span style="font-size: 20px; font-weight: bold; color: #1a1a2e; margin-left: 8px; vertical-align: middle;">ReplyIQ</span>
                  </div>
                  <h2 style="color: #1a1a2e; font-size: 22px; margin-bottom: 16px; text-align: center;">Password reset successful</h2>
                  <p style="color: #6b7280; font-size: 14px; line-height: 1.6; text-align: center;">
                    Your ReplyIQ password has been successfully reset. You can now log in with your new password.
                  </p>
                  <p style="color: #9ca3af; font-size: 12px; line-height: 1.5; margin-top: 24px; text-align: center;">
                    If you did not make this change, please contact us immediately at stephanie@erie-apps.com.
                  </p>
                  <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 32px 0 16px;">
                  <p style="color: #d1d5db; font-size: 11px; text-align: center;">&copy; 2026 Erie Apps LLC</p>
                </div>
                """;
    }
}
