package com.replyiq.controller;

import com.replyiq.dto.LocationResponse;
import com.replyiq.model.Location;
import com.replyiq.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationRepository locationRepository;

    @GetMapping
    public ResponseEntity<List<LocationResponse>> getLocations(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        List<LocationResponse> locations = locationRepository.findByUserId(userId)
                .stream()
                .map(LocationResponse::from)
                .toList();
        return ResponseEntity.ok(locations);
    }

    @PatchMapping("/{id}/settings")
    public ResponseEntity<LocationResponse> updateSettings(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates,
            Authentication auth) {

        Long userId = (Long) auth.getPrincipal();
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Location not found"));

        // Verify ownership
        if (!location.getUser().getId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }

        if (updates.containsKey("tonePreference")) {
            String tone = (String) updates.get("tonePreference");
            if (!List.of("professional", "friendly", "brief").contains(tone)) {
                throw new IllegalArgumentException("Tone must be: professional, friendly, or brief");
            }
            location.setTonePreference(tone);
        }

        if (updates.containsKey("autoPost")) {
            location.setAutoPost((Boolean) updates.get("autoPost"));
        }

        location = locationRepository.save(location);
        return ResponseEntity.ok(LocationResponse.from(location));
    }
}
