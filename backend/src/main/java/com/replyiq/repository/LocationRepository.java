package com.replyiq.repository;

import com.replyiq.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Long> {
    List<Location> findByUserId(Long userId);
}
