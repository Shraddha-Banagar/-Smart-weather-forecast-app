package com.smartweather.service;

import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartweather.dto.AnalyticsDtos.ChartDataResponse;
import com.smartweather.dto.AnalyticsDtos.HistoryPoint;
import com.smartweather.dto.AnalyticsDtos.TrendsResponse;
import com.smartweather.dto.ForecastDay;
import com.smartweather.dto.ForecastResponse;
import com.smartweather.entity.HistoricalWeather;
import com.smartweather.repository.HistoricalWeatherRepository;

/**
 * Phase 3: compares today's forecast against historical averages and serves
 * combined history + forecast payloads for the charts.
 */
@Service
public class AnalyticsService {

    private final WeatherService weatherService;
    private final HistoricalWeatherRepository history;

    public AnalyticsService(WeatherService weatherService, HistoricalWeatherRepository history) {
        this.weatherService = weatherService;
        this.history = history;
    }

    @Transactional(readOnly = true)
    public TrendsResponse getTrends(String rawCity) {
        // Validates the city first (404 for unknown) — forecast is 10-min cached.
        ForecastResponse forecast = weatherService.getForecast(rawCity);
        var normalized = forecast.city().strip().toLowerCase();
        List<HistoricalWeather> rows = history.findByCityNormalizedOrderByObservedDateAsc(normalized);

        // Java 21: SequencedCollection#getFirst() — day 0 is "today".
        ForecastDay today = forecast.daily().getFirst();

        if (rows.isEmpty()) {
            return new TrendsResponse(forecast.city(), forecast.country(), 0,
                    null, null, null, null, today, null,
                    "No historical observations for %s yet — pin the city and run collection, then check back."
                            .formatted(forecast.city()));
        }

        DoubleSummaryStatistics tempStats = rows.stream()
                .map(HistoricalWeather::getTemperatureC)
                .filter(t -> t != null)
                .mapToDouble(Double::doubleValue)
                .summaryStatistics();
        double avgHumidity = rows.stream()
                .map(HistoricalWeather::getHumidityPct)
                .filter(h -> h != null)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(Double.NaN);

        double avgTemp = tempStats.getAverage();
        double todayAvg = (today.tempMaxC() + today.tempMinC()) / 2.0;
        // Round to 1 decimal for a stable, readable API.
        avgTemp = Math.round(avgTemp * 10.0) / 10.0;
        double delta = Math.round((todayAvg - avgTemp) * 10.0) / 10.0;

        String verdict = describeDelta(delta, forecast.city());
        var first = rows.getFirst().getObservedDate();
        var last = rows.getLast().getObservedDate();
        Double humidityOrNull = Double.isNaN(avgHumidity) ? null
                : Math.round(avgHumidity * 10.0) / 10.0;

        return new TrendsResponse(forecast.city(), forecast.country(), rows.size(),
                first, last, avgTemp, humidityOrNull, today, delta, verdict);
    }

    @Transactional(readOnly = true)
    public ChartDataResponse getChartData(String rawCity, int days) {
        ForecastResponse forecast = weatherService.getForecast(rawCity);
        var normalized = forecast.city().strip().toLowerCase();
        List<HistoricalWeather> rows = history.findByCityNormalizedOrderByObservedDateAsc(normalized);

        // Most recent `days` observations; Streams + Optional for null-safe mapping.
        List<HistoryPoint> points = rows.stream()
                .skip(Math.max(0, rows.size() - days))
                .map(r -> new HistoryPoint(
                        r.getObservedDate(),
                        r.getTemperatureC(),
                        r.getHumidityPct(),
                        r.getWeatherCode()))
                .toList();

        return new ChartDataResponse(forecast.city(), forecast.country(), points, forecast.daily());
    }

    /** Verdict wording from the today-vs-history delta (switch expression). */
    static String describeDelta(double delta, String city) {
        String label = switch ((int) Math.signum(delta)) {
            case 1 -> delta >= 5 ? "much warmer than usual"
                    : delta >= 2 ? "warmer than usual" : "roughly in line with history";
            case -1 -> delta <= -5 ? "much colder than usual"
                    : delta <= -2 ? "colder than usual" : "roughly in line with history";
            default -> "exactly at its historical average";
        };
        return "Today in %s averages %+.1f°C vs. history — %s.".formatted(city, delta, label);
    }

    /** Helper for the recommendation service (empty when no history). */
    @Transactional(readOnly = true)
    public Optional<TrendsResponse> trendsIfAvailable(String city) {
        try {
            var trends = getTrends(city);
            return trends.sampleSize() == 0 ? Optional.empty() : Optional.of(trends);
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }
}
