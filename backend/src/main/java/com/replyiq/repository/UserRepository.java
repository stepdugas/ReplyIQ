package com.replyiq.repository;

import com.replyiq.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByStripeCustomerId(String stripeCustomerId);
    List<User> findBySubscriptionStatusIn(List<String> statuses);
}
