package com.smartweather.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartweather.dto.AnalyticsDtos.CollectResult;
import com.smartweather.dto.CurrentWeatherResponse;
import com.smartweather.entity.HistoricalWeather;
import com.smartweather.repository.HistoricalWeatherRepository;
import com.smartweather.repository.SavedCityRepository;

/**
 * Phase 3: background job that periodically snapshots observed weather for
 * every favored city into {@code historical_weather} (upsert per city/day).
 *
 * <p>Schedule is configurable via {@code app.history.*} properties; a manual
 * trigger exists at {@code POST /api/analytics/collect} for demos.
 */
@Service
public class HistoryCollectorService {

    private static final Logger log = LoggerFactory.getLogger(HistoryCollectorService.class);

    private final SavedCityRepository savedCities;
    private final HistoricalWeatherRepository history;
    private final WeatherService weatherService;

    public HistoryCollectorService(SavedCityRepository savedCities,
            HistoricalWeatherRepository history, WeatherService weatherService) {
        this.savedCities = savedCities;
        this.history = history;
        this.weatherService = weatherService;
    }

    @Scheduled(
            fixedRateString = "${app.history.collect-fixed-rate-ms:3600000}",
            initialDelayString = "${app.history.collect-initial-delay-ms:60000}")
    public void scheduledCollect() {
        var result = collectForFavoredCities();
        log.info("History collection: attempted={}, succeeded={}, failures={}",
                result.attempted(), result.succeeded(), result.failures().size());
    }

    @Transactional
    public CollectResult collectForFavoredCities() {
        // Distinct favored cities (case-insensitive) → display name per city.
        Map<String, String> cities = savedCities.findAll().stream()
                .filter(sc -> sc.getCity() != null && !sc.getCity().isBlank())
                .collect(Collectors.toMap(
                        sc -> sc.getCity().strip().toLowerCase(),
                        sc -> sc.getCity().strip(),
                        (first, second) -> first));
        return collectAll(cities);
    }

    @Transactional
    public CollectResult collectCity(String rawCity) {
        if (rawCity == null || rawCity.isBlank()) {
            throw new IllegalArgumentException("city must not be blank");
        }
        var city = rawCity.strip();
        return collectAll(Map.of(city.toLowerCase(), city));
    }

    private CollectResult collectAll(Map<String, String> cities) {
        List<String> failures = new ArrayList<>();
        int succeeded = 0;
        for (var city : cities.values()) {
            try {
                collectOne(city);
                succeeded++;
            } catch (RuntimeException e) {
                failures.add("%s: %s".formatted(city, e.getMessage()));
                log.warn("Failed to collect weather for '{}': {}", city, e.getMessage());
            }
        }
        return new CollectResult(Instant.now(), cities.size(), succeeded, List.copyOf(failures));
    }

    private void collectOne(String city) {
        // Goes through the cached proxy; geocoding validates the city (404 if unknown).
        CurrentWeatherResponse current = weatherService.getCurrentWeather(city);
        var today = LocalDate.now();
        var normalized = current.city().strip().toLowerCase();

        var existing = history.findByCityNormalizedAndObservedDate(normalized, today);
        if (existing.isPresent()) {
            existing.get().refresh(
                    current.temperatureC(), current.feelsLikeC(), current.humidityPct(),
                    current.windSpeedKph(), current.pressureHpa(),
                    current.weatherCode(), current.description());
        } else {
            history.save(new HistoricalWeather(
                    current.city(), current.country(), current.latitude(), current.longitude(),
                    today, current.temperatureC(), current.feelsLikeC(), current.humidityPct(),
                    current.windSpeedKph(), current.pressureHpa(),
                    current.weatherCode(), current.description()));
        }
    }
}
