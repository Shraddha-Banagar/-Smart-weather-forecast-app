package com.smartweather.config;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * Phase 2: 10-minute TTL cache so repeated lookups for the same city
 * don't spam the external provider. Cache names must match the
 * {@code @Cacheable} values in {@code WeatherService}.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final List<String> CACHE_NAMES = List.of("currentWeather", "forecast");

    @Bean
    CacheManager cacheManager() {
        var manager = new CaffeineCacheManager();
        manager.setCacheNames(CACHE_NAMES);
        manager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(1_000)
                .recordStats());
        return manager;
    }
}
