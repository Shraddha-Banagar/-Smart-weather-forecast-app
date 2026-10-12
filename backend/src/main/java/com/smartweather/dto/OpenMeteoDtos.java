package com.smartweather.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Internal wire-format DTOs for the Open-Meteo APIs.
 * Kept separate from the public API records so upstream changes don't leak out.
 */
public final class OpenMeteoDtos {

    private OpenMeteoDtos() {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GeocodingApiResponse(List<GeocodingResult> results) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GeocodingResult(
            String name,
            String country,
            Double latitude,
            Double longitude
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ForecastApiResponse(
            Double latitude,
            Double longitude,
            String timezone,
            CurrentBlock current,
            DailyBlock daily
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CurrentBlock(
            String time,
            Double temperature_2m,
            Double apparent_temperature,
            Integer relative_humidity_2m,
            Integer weather_code,
            Double wind_speed_10m,
            Integer wind_direction_10m,
            Double pressure_msl
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DailyBlock(
            List<String> time,
            List<Integer> weather_code,
            List<Double> temperature_2m_max,
            List<Double> temperature_2m_min,
            List<Integer> precipitation_probability_max
    ) {}
}
