package com.replyiq.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.replyiq.model.Location;
import com.replyiq.model.User;
import com.replyiq.repository.LocationRepository;
import com.replyiq.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoogleBusinessService {

    private final GoogleOAuthService googleOAuthService;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String GBP_API_BASE = "https://mybusinessbusinessinformation.googleapis.com/v1";
    private static final String ACCOUNT_API_BASE = "https://mybusinessaccountmanagement.googleapis.com/v1";

    /**
     * Fetches all Google Business Profile accounts for the user,
     * then fetches locations under each account and stores them.
     */
    public List<Location> fetchAndStoreLocations(Long userId) {
        String accessToken = googleOAuthService.getValidAccessToken(userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Step 1: Get accounts
        List<String> accountNames = fetchAccountNames(accessToken);

        // Step 2: Get locations for each account
        List<Location> allLocations = new ArrayList<>();
        for (String accountName : accountNames) {
            List<Location> locations = fetchLocationsForAccount(accountName, accessToken, user);
            allLocations.addAll(locations);
        }

        System.out.println("SUCCESS: Fetched " + allLocations.size() + " locations for user " + userId);
        return allLocations;
    }

    private List<String> fetchAccountNames(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<String> response = restTemplate.exchange(
                ACCOUNT_API_BASE + "/accounts",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        List<String> accountNames = new ArrayList<>();
        try {
            JsonNode json = objectMapper.readTree(response.getBody());
            JsonNode accounts = json.get("accounts");
            if (accounts != null && accounts.isArray()) {
                for (JsonNode account : accounts) {
                    accountNames.add(account.get("name").asText());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch Google Business accounts: " + e.getMessage(), e);
        }
        return accountNames;
    }

    private List<Location> fetchLocationsForAccount(String accountName, String accessToken, User user) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        String url = GBP_API_BASE + "/" + accountName + "/locations?readMask=name,title,storefrontAddress";

        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        List<Location> locations = new ArrayList<>();
        try {
            JsonNode json = objectMapper.readTree(response.getBody());
            JsonNode locationNodes = json.get("locations");
            if (locationNodes != null && locationNodes.isArray()) {
                for (JsonNode loc : locationNodes) {
                    String googleLocationId = loc.get("name").asText();
                    String title = loc.has("title") ? loc.get("title").asText() : "Unnamed Location";

                    String address = "";
                    if (loc.has("storefrontAddress")) {
                        JsonNode addr = loc.get("storefrontAddress");
                        List<String> parts = new ArrayList<>();
                        if (addr.has("addressLines")) {
                            for (JsonNode line : addr.get("addressLines")) {
                                parts.add(line.asText());
                            }
                        }
                        if (addr.has("locality")) parts.add(addr.get("locality").asText());
                        if (addr.has("administrativeArea")) parts.add(addr.get("administrativeArea").asText());
                        if (addr.has("postalCode")) parts.add(addr.get("postalCode").asText());
                        address = String.join(", ", parts);
                    }

                    // Upsert: don't duplicate if already connected
                    Location existing = locationRepository
                            .findByUserId(user.getId()).stream()
                            .filter(l -> l.getGoogleLocationId().equals(googleLocationId))
                            .findFirst()
                            .orElse(null);

                    if (existing == null) {
                        Location location = Location.builder()
                                .user(user)
                                .googleLocationId(googleLocationId)
                                .locationName(title)
                                .address(address)
                                .build();
                        locations.add(locationRepository.save(location));
                    } else {
                        existing.setLocationName(title);
                        existing.setAddress(address);
                        locations.add(locationRepository.save(existing));
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch locations for " + accountName + ": " + e.getMessage(), e);
        }
        return locations;
    }
}
