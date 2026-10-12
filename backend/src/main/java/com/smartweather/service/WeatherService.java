package com.smartweather.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.smartweather.dto.CurrentWeatherResponse;
import com.smartweather.dto.ForecastDay;
import com.smartweather.dto.ForecastResponse;
import com.smartweather.dto.OpenMeteoDtos;
import com.smartweather.exception.CityNotFoundException;
import com.smartweather.exception.ExternalWeatherApiException;

/**
 * Level 1 weather proxy. All external calls go through here —
 * the frontend never touches Open-Meteo directly.
 *
 * <p>Showcases Java 21: Records, {@link Optional}, Streams, switch expressions,
 * and pattern matching.
 */
@Service
public class WeatherService {

    private final RestClient restClient;
    private final String geocodingUrl;
    private final String forecastUrl;

    public WeatherService(
            RestClient weatherRestClient,
            @Value("${app.weather.geocoding-url}") String geocodingUrl,
            @Value("${app.weather.forecast-url}") String forecastUrl) {
        this.restClient = weatherRestClient;
        this.geocodingUrl = geocodingUrl;
        this.forecastUrl = forecastUrl;
    }

    // Phase 2: 10-minute TTL cache (see CacheConfig) — same city within the
    // window is served from Caffeine without hitting Open-Meteo.
    @Cacheable(value = "currentWeather", key = "#city.strip().toLowerCase()")
    public CurrentWeatherResponse getCurrentWeather(String city) {
        var trimmed = requireCity(city);
        var location = resolveLocation(trimmed);

        OpenMeteoDtos.ForecastApiResponse api = fetchForecast(
                location.latitude(), location.longitude(), true);

        var current = Optional.ofNullable(api.current())
                .orElseThrow(() -> new ExternalWeatherApiException("Provider returned no 'current' block"));

        double temp = orZero(current.temperature_2m());
        return new CurrentWeatherResponse(
                location.name(),
                Optional.ofNullable(location.country()).orElse(""),
                location.latitude(),
                location.longitude(),
                temp,
                orZero(current.apparent_temperature()),
                orZero(current.relative_humidity_2m()),
                orZero(current.wind_speed_10m()),
                orZero(current.wind_direction_10m()),
                orZero(current.pressure_msl()),
                orZero(current.weather_code()),
                describeWeatherCode(orZero(current.weather_code())),
                Instant.now());
    }

    @Cacheable(value = "forecast", key = "#city.strip().toLowerCase()")
    public ForecastResponse getForecast(String city) {
        var trimmed = requireCity(city);
        var location = resolveLocation(trimmed);

        OpenMeteoDtos.ForecastApiResponse api = fetchForecast(
                location.latitude(), location.longitude(), false);

        var daily = Optional.ofNullable(api.daily())
                .orElseThrow(() -> new ExternalWeatherApiException("Provider returned no 'daily' block"));

        int days = Optional.ofNullable(daily.time()).map(List::size).orElse(0);
        if (days == 0) {
            throw new ExternalWeatherApiException("Provider returned an empty forecast");
        }

        // Streams + IntStream to zip parallel daily arrays into ForecastDay records.
        List<ForecastDay> forecastDays = IntStream.range(0, days)
                .mapToObj(i -> new ForecastDay(
                        safeGet(daily.time(), i).orElse(""),
                        safeGet(daily.temperature_2m_max(), i).orElse(0.0),
                        safeGet(daily.temperature_2m_min(), i).orElse(0.0),
                        safeGet(daily.precipitation_probability_max(), i).orElse(0),
                        safeGet(daily.weather_code(), i).orElse(0),
                        describeWeatherCode(safeGet(daily.weather_code(), i).orElse(0))))
                .toList();

        return new ForecastResponse(
                location.name(),
                Optional.ofNullable(location.country()).orElse(""),
                location.latitude(),
                location.longitude(),
                Optional.ofNullable(api.timezone()).orElse("auto"),
                forecastDays);
    }

    // ── internals ──────────────────────────────────────────────

    private OpenMeteoDtos.GeocodingResult resolveLocation(String city) {
        try {
            var response = restClient.get()
                    .uri(geocodingUrl + "?name={city}&count=1&language=en&format=json", city)
                    .retrieve()
                    .body(OpenMeteoDtos.GeocodingApiResponse.class);

            // Optional + Streams: first usable result or 404.
            return Optional.ofNullable(response)
                    .map(OpenMeteoDtos.GeocodingApiResponse::results)
                    .flatMap(results -> results.stream().findFirst())
                    .filter(r -> r.latitude() != null && r.longitude() != null)
                    .orElseThrow(() -> new CityNotFoundException(city));
        } catch (CityNotFoundException e) {
            throw e;
        } catch (RestClientException e) {
            throw new ExternalWeatherApiException("Geocoding lookup failed for '%s'".formatted(city), e);
        }
    }

    private OpenMeteoDtos.ForecastApiResponse fetchForecast(double lat, double lon, boolean currentOnly) {
        String uri = currentOnly
                ? forecastUrl + "?latitude={lat}&longitude={lon}&current=temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m,wind_direction_10m,pressure_msl&timezone=auto"
                : forecastUrl + "?latitude={lat}&longitude={lon}&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max&timezone=auto&forecast_days=5";
        try {
            return Optional.ofNullable(restClient.get()
                            .uri(uri, lat, lon)
                            .retrieve()
                            .body(OpenMeteoDtos.ForecastApiResponse.class))
                    .orElseThrow(() -> new ExternalWeatherApiException("Empty response from provider"));
        } catch (RestClientException e) {
            throw new ExternalWeatherApiException("Forecast fetch failed (lat=%s, lon=%s)".formatted(lat, lon), e);
        }
    }

    private static String requireCity(String city) {
        // Pattern matching: null / blank rejected before any outbound call.
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("Query param 'city' must not be blank");
        }
        return city.strip();
    }

    private static <T> Optional<T> safeGet(List<T> list, int index) {
        return Optional.ofNullable(list).filter(l -> index < l.size()).map(l -> l.get(index));
    }

    private static double orZero(Double v) {
        return Optional.ofNullable(v).orElse(0.0);
    }

    private static int orZero(Integer v) {
        return Optional.ofNullable(v).orElse(0);
    }

    /** WMO weather-code → human label via switch expression. */
    static String describeWeatherCode(int code) {
        return switch (code) {
            case 0 -> "Clear sky";
            case 1 -> "Mainly clear";
            case 2 -> "Partly cloudy";
            case 3 -> "Overcast";
            case 45, 48 -> "Foggy";
            case 51, 53, 55 -> "Drizzle";
            case 56, 57 -> "Freezing drizzle";
            case 61 -> "Light rain";
            case 63 -> "Rain";
            case 65 -> "Heavy rain";
            case 66, 67 -> "Freezing rain";
            case 71 -> "Light snow";
            case 73 -> "Snow";
            case 75 -> "Heavy snow";
            case 77 -> "Snow grains";
            case 80, 81, 82 -> "Rain showers";
            case 85, 86 -> "Snow showers";
            case 95 -> "Thunderstorm";
            case 96, 99 -> "Thunderstorm with hail";
            default -> "Unknown";
        };
    }
}
