package com.smartweather.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartweather.dto.CurrentWeatherResponse;
import com.smartweather.dto.ForecastResponse;
import com.smartweather.service.SearchHistoryService;
import com.smartweather.service.WeatherService;

import jakarta.validation.constraints.NotBlank;

/**
 * Weather proxy. An optional {@code X-User-Id} header records each lookup
 * into that user's search history (anonymous calls still work).
 *
 * <p>History tracking is best-effort: a stale user id or a down database
 * must never break the weather response itself.
 */
@Validated
@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private static final Logger log = LoggerFactory.getLogger(WeatherController.class);

    private final WeatherService weatherService;
    private final SearchHistoryService historyService;

    public WeatherController(WeatherService weatherService, SearchHistoryService historyService) {
        this.weatherService = weatherService;
        this.historyService = historyService;
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Weather API is up");
    }

    @GetMapping("/current")
    public ResponseEntity<CurrentWeatherResponse> current(
            @RequestParam @NotBlank(message = "city must not be blank") String city,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        var result = weatherService.getCurrentWeather(city);
        recordHistoryQuietly(userId, result.city());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/forecast")
    public ResponseEntity<ForecastResponse> forecast(
            @RequestParam @NotBlank(message = "city must not be blank") String city,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        var result = weatherService.getForecast(city);
        recordHistoryQuietly(userId, result.city());
        return ResponseEntity.ok(result);
    }

    private void recordHistoryQuietly(Long userId, String city) {
        if (userId == null) {
            return;
        }
        try {
            historyService.record(userId, city);
        } catch (RuntimeException e) {
            // Stale user id, DB outage, etc. — log and serve weather anyway.
            log.warn("Skipping search-history record for user {} ({}): {}", userId, city, e.getMessage());
        }
    }
}
