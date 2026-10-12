package com.smartweather.dto;

import java.time.Instant;

/** Public API: current weather snapshot for a city. Java 21 Record DTO. */
public record CurrentWeatherResponse(
        String city,
        String country,
        double latitude,
        double longitude,
        double temperatureC,
        double feelsLikeC,
        int humidityPct,
        double windSpeedKph,
        int windDirectionDeg,
        double pressureHpa,
        int weatherCode,
        String description,
        Instant observedAt
) {}
