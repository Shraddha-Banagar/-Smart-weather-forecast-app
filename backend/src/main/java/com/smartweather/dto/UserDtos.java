package com.smartweather.dto;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Phase 2: mock auth + favorites + history DTOs (all Records). */
public final class UserDtos {

    private UserDtos() {}

    public record LoginRequest(@NotBlank(message = "username must not be blank") @Size(max = 50) String username) {}

    public record UserResponse(Long id, String username, String displayName, Instant createdAt) {}

    public record SaveCityRequest(@NotBlank(message = "city must not be blank") @Size(max = 100) String city) {}

    public record SavedCityResponse(
            Long id,
            String city,
            String country,
            Double latitude,
            Double longitude,
            Instant createdAt
    ) {}

    public record SearchHistoryResponse(Long id, String city, Instant createdAt) {}
}
