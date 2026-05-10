package com.replyiq.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimiter rateLimiter;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        String method = request.getMethod();

        if (!"POST".equals(method)) return true;

        String ip = getClientIp(request);

        if ("/api/auth/signup".equals(path)) {
            if (!rateLimiter.isAllowed("signup:" + ip, 5, 3600)) {
                writeRateLimitResponse(response, "Too many signup attempts. Please try again later.");
                return false;
            }
        } else if ("/api/auth/login".equals(path)) {
            if (!rateLimiter.isAllowed("login:" + ip, 10, 900)) {
                writeRateLimitResponse(response, "Too many login attempts. Please try again in a few minutes.");
                return false;
            }
        }

        return true;
    }

    private void writeRateLimitResponse(HttpServletResponse response, String message) throws Exception {
        response.setStatus(429);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), Map.of("error", message));
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
