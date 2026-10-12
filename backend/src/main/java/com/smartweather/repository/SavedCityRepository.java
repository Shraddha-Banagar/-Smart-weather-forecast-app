package com.smartweather.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartweather.entity.SavedCity;

public interface SavedCityRepository extends JpaRepository<SavedCity, Long> {

    List<SavedCity> findByUserIdOrderByCreatedAtAsc(Long userId);

    Optional<SavedCity> findByUserIdAndCityNormalized(Long userId, String cityNormalized);

    void deleteByUserIdAndCityNormalized(Long userId, String cityNormalized);
}
