package com.smartweather.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartweather.entity.HistoricalWeather;

public interface HistoricalWeatherRepository extends JpaRepository<HistoricalWeather, Long> {

    List<HistoricalWeather> findByCityNormalizedOrderByObservedDateAsc(String cityNormalized);

    Optional<HistoricalWeather> findByCityNormalizedAndObservedDate(String cityNormalized, LocalDate observedDate);
}
