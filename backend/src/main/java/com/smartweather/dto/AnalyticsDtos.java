package com.smartweather.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** Phase 3: trends, chart data, recommendations, collector result (all Records). */
public final class AnalyticsDtos {

    private AnalyticsDtos() {}

    /** Today's forecast vs. historical averages for a city. Null averages = no history yet. */
    public record TrendsResponse(
            String city,
            String country,
            int sampleSize,
            LocalDate periodStart,
            LocalDate periodEnd,
            Double avgTempC,
            Double avgHumidityPct,
            ForecastDay today,
            Double deltaTodayAvgVsHistory,
            String verdict
    ) {}

    /** One observed snapshot for charting. */
    public record HistoryPoint(
            LocalDate date,
            Double tempC,
            Integer humidityPct,
            Integer weatherCode
    ) {}

    /** Observed history + 5-day forecast in one payload for the charts. */
    public record ChartDataResponse(
            String city,
            String country,
            List<HistoryPoint> history,
            List<ForecastDay> forecast
    ) {}

    /** One actionable advice item. Severity: {@code info} | {@code warning} | {@code alert}. */
    public record Advice(String severity, String message) {}

    public record RecommendationResponse(
            String city,
            Instant generatedAt,
            List<Advice> advice
    ) {}

    /** Outcome of an observation-collection run. */
    public record CollectResult(
            Instant collectedAt,
            int attempted,
            int succeeded,
            List<String> failures
    ) {}
}
