package com.replyiq.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "locations")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "google_location_id", nullable = false)
    private String googleLocationId;

    @Column(name = "location_name", nullable = false)
    private String locationName;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "tone_preference", nullable = false)
    @Builder.Default
    private String tonePreference = "professional";

    @Column(name = "auto_post", nullable = false)
    @Builder.Default
    private Boolean autoPost = false;

    @Column(name = "connected_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime connectedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();
}
