package com.replyiq.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "nurture_emails")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class NurtureEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "email_day", nullable = false)
    private Integer emailDay;

    @Column(name = "sent_at", nullable = false)
    @Builder.Default
    private LocalDateTime sentAt = LocalDateTime.now();
}
