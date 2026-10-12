package com.smartweather.dto;

/** Public API: one day of the 5-day forecast. */
public record ForecastDay(
        String date,
        double tempMaxC,
        double tempMinC,
        int precipitationProbabilityPct,
        int weatherCode,
        String description
) {}
