package com.replyiq.repository;

import com.replyiq.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByLocationIdOrderByPostedAtDesc(Long locationId);

    List<Review> findByReplyStatusOrderByPostedAtDesc(String replyStatus);

    Optional<Review> findByGoogleReviewId(String googleReviewId);

    @Query("SELECT r FROM Review r JOIN FETCH r.location WHERE r.location.user.id = :userId ORDER BY r.postedAt DESC")
    List<Review> findAllByUserId(Long userId);

    @Query("SELECT r FROM Review r JOIN FETCH r.location WHERE r.location.user.id = :userId AND r.replyStatus = :status ORDER BY r.postedAt DESC")
    List<Review> findByUserIdAndStatus(Long userId, String status);

    @Query("SELECT r FROM Review r JOIN FETCH r.location WHERE r.location.user.id = :userId ORDER BY r.postedAt DESC")
    Page<Review> findAllByUserId(Long userId, Pageable pageable);

    @Query("SELECT r FROM Review r JOIN FETCH r.location WHERE r.location.user.id = :userId AND r.replyStatus = :status ORDER BY r.postedAt DESC")
    Page<Review> findByUserIdAndStatus(Long userId, String status, Pageable pageable);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.location.user.id = :userId")
    long countByUserId(Long userId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.location.user.id = :userId AND r.replyStatus = 'needs_reply'")
    long countUnansweredByUserId(Long userId);

    @Query("SELECT AVG(r.starRating) FROM Review r WHERE r.location.user.id = :userId")
    Double averageRatingByUserId(Long userId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.location.user.id = :userId AND r.replyStatus = 'posted' AND r.repliedAt >= :since")
    long countRepliedSince(Long userId, java.time.LocalDateTime since);
}
