package com.replyiq.repository;

import com.replyiq.model.NurtureEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NurtureEmailRepository extends JpaRepository<NurtureEmail, Long> {
    List<NurtureEmail> findByUserId(Long userId);
    boolean existsByUserIdAndEmailDay(Long userId, Integer emailDay);
}
