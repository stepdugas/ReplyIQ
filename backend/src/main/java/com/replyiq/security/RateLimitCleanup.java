package com.replyiq.security;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RateLimitCleanup {

    private final RateLimiter rateLimiter;

    @Scheduled(fixedRate = 300000) // every 5 minutes
    public void cleanup() {
        rateLimiter.cleanup();
    }
}
