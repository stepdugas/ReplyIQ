package com.replyiq.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Column(name = "google_review_id", nullable = false, unique = true)
    private String googleReviewId;

    @Column(name = "reviewer_name", nullable = false)
    private String reviewerName;

    @Column(name = "star_rating", nullable = false)
    private Integer starRating;

    @Column(name = "review_text", columnDefinition = "TEXT")
    private String reviewText;

    @Column(name = "posted_at", nullable = false)
    private LocalDateTime postedAt;

    @Column(name = "reply_text", columnDefinition = "TEXT")
    private String replyText;

    @Column(name = "reply_status", nullable = false)
    @Builder.Default
    private String replyStatus = "needs_reply";

    @Column(name = "replied_at")
    private LocalDateTime repliedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
