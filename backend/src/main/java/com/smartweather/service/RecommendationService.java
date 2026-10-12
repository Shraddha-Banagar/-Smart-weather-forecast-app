package com.smartweather.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.smartweather.dto.AnalyticsDtos.Advice;
import com.smartweather.dto.AnalyticsDtos.RecommendationResponse;
import com.smartweather.dto.CurrentWeatherResponse;
import com.smartweather.dto.ForecastDay;
import com.smartweather.dto.ForecastResponse;

/**
 * Phase 3: deterministic, rule-based advice from current conditions, the 5-day
 * forecast, and (when available) historical trends. No ML — every rule is a
 * readable threshold below.
 */
@Service
public class RecommendationService {

    private static final Set<Integer> THUNDER_CODES = Set.of(95, 96, 99);

    private final WeatherService weatherService;
    private final AnalyticsService analyticsService;

    public RecommendationService(WeatherService weatherService, AnalyticsService analyticsService) {
        this.weatherService = weatherService;
        this.analyticsService = analyticsService;
    }

    public RecommendationResponse recommend(String rawCity) {
        CurrentWeatherResponse current = weatherService.getCurrentWeather(rawCity);
        ForecastResponse forecast = weatherService.getForecast(rawCity);
        // Java 21 SequencedCollection accessors.
        ForecastDay today = forecast.daily().getFirst();
        ForecastDay tomorrow = forecast.daily().size() > 1 ? forecast.daily().get(1) : today;

        List<Advice> advice = new ArrayList<>();

        // 1. Severe weather anywhere in the outlook (highest priority).
        forecast.daily().stream()
                .filter(d -> THUNDER_CODES.contains(d.weatherCode()))
                .findFirst()
                .ifPresent(stormy -> advice.add(new Advice("alert",
                        "Thunderstorms expected around %s (%s) — avoid outdoor plans and secure loose items."
                                .formatted(stormy.date(), stormy.description()))));

        // 2. Rainfall for today.
        if (today.precipitationProbabilityPct() >= 70) {
            advice.add(new Advice("warning",
                    "High rain chance today (%d%%) — carry an umbrella and allow extra commute time."
                            .formatted(today.precipitationProbabilityPct())));
        } else if (today.precipitationProbabilityPct() >= 40) {
            advice.add(new Advice("info",
                    "Moderate rain chance today (%d%%) — keep an umbrella handy."
                            .formatted(today.precipitationProbabilityPct())));
        }

        // 3. Sharp overnight temperature drop (today's high vs tomorrow's low).
        double drop = today.tempMaxC() - tomorrow.tempMinC();
        if (drop >= 8) {
            advice.add(new Advice("warning",
                    "Sharp temperature drop expected tonight (%.0f° → %.0f°C) — bring a jacket."
                            .formatted(today.tempMaxC(), tomorrow.tempMinC())));
        }

        // 4. Heat / cold extremes from current + forecast.
        if (current.temperatureC() >= 32 || today.tempMaxC() >= 35) {
            advice.add(new Advice("warning",
                    "Heat expected (up to %.0f°C) — hydrate, seek shade midday, and check on vulnerable people."
                            .formatted(Math.max(current.temperatureC(), today.tempMaxC()))));
        } else if (current.temperatureC() <= 0 || today.tempMinC() <= -3) {
            advice.add(new Advice("alert",
                    "Freezing conditions (down to %.0f°C) — dress warmly and watch for ice on roads."
                            .formatted(Math.min(current.temperatureC(), today.tempMinC()))));
        } else if (today.tempMinC() <= 5) {
            advice.add(new Advice("info",
                    "Chilly morning expected (%.0f°C) — grab a warm layer.".formatted(today.tempMinC())));
        }

        // 5. Wind.
        if (current.windSpeedKph() >= 60) {
            advice.add(new Advice("alert",
                    "Dangerous winds (%.0f km/h) — postpone cycling/sailing and secure outdoor objects."
                            .formatted(current.windSpeedKph())));
        } else if (current.windSpeedKph() >= 40) {
            advice.add(new Advice("warning",
                    "Strong winds (%.0f km/h) — expect difficult cycling and rough seas."
                            .formatted(current.windSpeedKph())));
        }

        // 6. Muggy combination.
        if (current.temperatureC() >= 28 && current.humidityPct() >= 80) {
            advice.add(new Advice("info",
                    "Hot and humid (%d%%) — it will feel muggier than %.0f°C suggests; light clothing helps."
                            .formatted(current.humidityPct(), current.temperatureC())));
        }

        // 7. Large diurnal swing → layers.
        if (today.tempMaxC() - today.tempMinC() >= 12) {
            advice.add(new Advice("info",
                    "Big day-night swing (%.0f° / %.0f°C) — dress in layers you can shed."
                            .formatted(today.tempMaxC(), today.tempMinC())));
        }

        // 8. History comparison (only when observations exist).
        analyticsService.trendsIfAvailable(current.city()).ifPresent(trends -> {
            Double delta = trends.deltaTodayAvgVsHistory();
            if (delta != null && delta >= 5) {
                advice.add(new Advice("info",
                        "Notably warmer than this city's recorded average (+%.1f°C) — plan activities for cooler hours."
                                .formatted(delta)));
            } else if (delta != null && delta <= -5) {
                advice.add(new Advice("info",
                        "Notably colder than this city's recorded average (%.1f°C) — heating and warm clothing advised."
                                .formatted(delta)));
            }
        });

        if (advice.isEmpty()) {
            advice.add(new Advice("info",
                    "Conditions look steady in %s — no warnings; a good window for outdoor plans."
                            .formatted(current.city())));
        }

        return new RecommendationResponse(current.city(), Instant.now(), List.copyOf(advice));
    }
}
