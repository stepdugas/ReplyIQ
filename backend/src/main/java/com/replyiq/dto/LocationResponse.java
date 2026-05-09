package com.replyiq.dto;

import com.replyiq.model.Location;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class LocationResponse {
    private Long id;
    private String googleLocationId;
    private String locationName;
    private String address;
    private String tonePreference;
    private boolean autoPost;
    private LocalDateTime connectedAt;

    public static LocationResponse from(Location location) {
        return LocationResponse.builder()
                .id(location.getId())
                .googleLocationId(location.getGoogleLocationId())
                .locationName(location.getLocationName())
                .address(location.getAddress())
                .tonePreference(location.getTonePreference())
                .autoPost(location.getAutoPost())
                .connectedAt(location.getConnectedAt())
                .build();
    }
}
