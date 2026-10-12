package com.smartweather.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartweather.dto.AnalyticsDtos.ChartDataResponse;
import com.smartweather.dto.AnalyticsDtos.CollectResult;
import com.smartweather.dto.AnalyticsDtos.RecommendationResponse;
import com.smartweather.dto.AnalyticsDtos.TrendsResponse;
import com.smartweather.service.AnalyticsService;
import com.smartweather.service.HistoryCollectorService;
import com.smartweather.service.RecommendationService;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Phase 3 intelligence API: trends, chart data, recommendations, and a manual
 * collection trigger (the automatic job runs on {@code app.history.*} schedule).
 */
@Validated
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final RecommendationService recommendationService;
    private final HistoryCollectorService collectorService;

    public AnalyticsController(AnalyticsService analyticsService,
            RecommendationService recommendationService, HistoryCollectorService collectorService) {
        this.analyticsService = analyticsService;
        this.recommendationService = recommendationService;
        this.collectorService = collectorService;
    }

    @GetMapping("/trends")
    public ResponseEntity<TrendsResponse> trends(
            @RequestParam @NotBlank(message = "city must not be blank") String city) {
        return ResponseEntity.ok(analyticsService.getTrends(city));
    }

    @GetMapping("/chart")
    public ResponseEntity<ChartDataResponse> chart(
            @RequestParam @NotBlank(message = "city must not be blank") String city,
            @RequestParam(defaultValue = "14") @Min(1) @Max(90) int days) {
        return ResponseEntity.ok(analyticsService.getChartData(city, days));
    }

    @GetMapping("/recommendations")
    public ResponseEntity<RecommendationResponse> recommendations(
            @RequestParam @NotBlank(message = "city must not be blank") String city) {
        return ResponseEntity.ok(recommendationService.recommend(city));
    }

    @PostMapping("/collect")
    public ResponseEntity<CollectResult> collect(
            @RequestParam(required = false) String city) {
        if (city == null || city.isBlank()) {
            return ResponseEntity.ok(collectorService.collectForFavoredCities());
        }
        return ResponseEntity.ok(collectorService.collectCity(city));
    }
}
