package com.smartweather.dto;

import java.util.List;

/** Public API: 5-day forecast wrapper. */
public record ForecastResponse(
        String city,
        String country,
        double latitude,
        double longitude,
        String timezone,
        List<ForecastDay> daily
) {}
