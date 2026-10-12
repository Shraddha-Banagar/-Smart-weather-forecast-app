package com.smartweather.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartweather.dto.CurrentWeatherResponse;
import com.smartweather.dto.UserDtos.SavedCityResponse;
import com.smartweather.entity.SavedCity;
import com.smartweather.exception.CityNotFoundException;
import com.smartweather.exception.DuplicateFavoriteException;
import com.smartweather.repository.SavedCityRepository;

/**
 * Phase 2: pin / unpin / list favorite cities.
 * Coordinates are resolved via the cached {@link WeatherService} so saving
 * a favorite also warms the weather cache.
 */
@Service
public class FavoritesService {

    private final SavedCityRepository savedCities;
    private final UserService userService;
    private final WeatherService weatherService;

    public FavoritesService(SavedCityRepository savedCities, UserService userService, WeatherService weatherService) {
        this.savedCities = savedCities;
        this.userService = userService;
        this.weatherService = weatherService;
    }

    @Transactional(readOnly = true)
    public List<SavedCityResponse> list(Long userId) {
        userService.requireUser(userId);
        return savedCities.findByUserIdOrderByCreatedAtAsc(userId).stream()
                .map(FavoritesService::toResponse)
                .toList();
    }

    @Transactional
    public SavedCityResponse add(Long userId, String rawCity) {
        var user = userService.requireUser(userId);
        if (rawCity == null || rawCity.isBlank()) {
            throw new IllegalArgumentException("city must not be blank");
        }
        var city = rawCity.strip();
        var normalized = city.toLowerCase();

        if (savedCities.findByUserIdAndCityNormalized(userId, normalized).isPresent()) {
            throw new DuplicateFavoriteException("'%s' is already in favorites".formatted(city));
        }

        // Validate the city + capture coordinates via the (cached) weather proxy.
        // 404 here propagates as CityNotFoundException → HTTP 404.
        CurrentWeatherResponse current;
        try {
            current = weatherService.getCurrentWeather(city);
        } catch (CityNotFoundException e) {
            throw new CityNotFoundException(city);
        }

        var saved = savedCities.save(new SavedCity(
                user, current.city(), current.country(), current.latitude(), current.longitude()));
        return toResponse(saved);
    }

    @Transactional
    public void remove(Long userId, String rawCity) {
        userService.requireUser(userId);
        if (rawCity == null || rawCity.isBlank()) {
            throw new IllegalArgumentException("city must not be blank");
        }
        var normalized = rawCity.strip().toLowerCase();
        var existing = savedCities.findByUserIdAndCityNormalized(userId, normalized)
                .orElseThrow(() -> new CityNotFoundException(rawCity.strip()));
        savedCities.delete(existing);
    }

    static SavedCityResponse toResponse(SavedCity entity) {
        return new SavedCityResponse(
                entity.getId(),
                entity.getCity(),
                entity.getCountry(),
                entity.getLatitude(),
                entity.getLongitude(),
                entity.getCreatedAt());
    }
}
