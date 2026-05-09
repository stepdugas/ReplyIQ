package com.replyiq.repository;

import com.replyiq.model.OAuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OAuthTokenRepository extends JpaRepository<OAuthToken, Long> {
    Optional<OAuthToken> findByUserId(Long userId);
}
