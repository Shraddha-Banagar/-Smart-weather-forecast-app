package com.smartweather.entity;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Phase 3: observed-weather snapshots, one row per (city, day).
 * Populated by the scheduled {@code HistoryCollectorService} for favored cities.
 * Table: {@code historical_weather}.
 */
@Entity
@Table(name = "historical_weather", uniqueConstraints = {
        @UniqueConstraint(name = "uq_history_city_date", columnNames = { "city_normalized", "observed_date" })
})
public class HistoricalWeather {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(name = "city_normalized", nullable = false, length = 100)
    private String cityNormalized;

    @Column(length = 100)
    private String country;

    private Double latitude;

    private Double longitude;

    @Column(name = "observed_date", nullable = false)
    private LocalDate observedDate;

    @Column(name = "observed_at", nullable = false)
    private Instant observedAt;

    @Column(name = "temperature_c")
    private Double temperatureC;

    @Column(name = "feels_like_c")
    private Double feelsLikeC;

    @Column(name = "humidity_pct")
    private Integer humidityPct;

    @Column(name = "wind_speed_kph")
    private Double windSpeedKph;

    @Column(name = "pressure_hpa")
    private Double pressureHpa;

    @Column(name = "weather_code")
    private Integer weatherCode;

    @Column(length = 50)
    private String description;

    protected HistoricalWeather() {}

    public HistoricalWeather(String city, String country, Double latitude, Double longitude,
            LocalDate observedDate, Double temperatureC, Double feelsLikeC, Integer humidityPct,
            Double windSpeedKph, Double pressureHpa, Integer weatherCode, String description) {
        this.city = city;
        this.cityNormalized = city.strip().toLowerCase();
        this.country = country;
        this.latitude = latitude;
        this.longitude = longitude;
        this.observedDate = observedDate;
        this.temperatureC = temperatureC;
        this.feelsLikeC = feelsLikeC;
        this.humidityPct = humidityPct;
        this.windSpeedKph = windSpeedKph;
        this.pressureHpa = pressureHpa;
        this.weatherCode = weatherCode;
        this.description = description;
    }

    @PrePersist
    void onCreate() {
        observedAt = Instant.now();
        if (cityNormalized == null && city != null) {
            cityNormalized = city.strip().toLowerCase();
        }
    }

    @PreUpdate
    void onUpdate() {
        observedAt = Instant.now();
    }

    /** Upsert path: refresh this row with a newer same-day observation. */
    public void refresh(Double temperatureC, Double feelsLikeC, Integer humidityPct,
            Double windSpeedKph, Double pressureHpa, Integer weatherCode, String description) {
        this.temperatureC = temperatureC;
        this.feelsLikeC = feelsLikeC;
        this.humidityPct = humidityPct;
        this.windSpeedKph = windSpeedKph;
        this.pressureHpa = pressureHpa;
        this.weatherCode = weatherCode;
        this.description = description;
    }

    public Long getId() { return id; }
    public String getCity() { return city; }
    public String getCityNormalized() { return cityNormalized; }
    public String getCountry() { return country; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public LocalDate getObservedDate() { return observedDate; }
    public Instant getObservedAt() { return observedAt; }
    public Double getTemperatureC() { return temperatureC; }
    public Double getFeelsLikeC() { return feelsLikeC; }
    public Integer getHumidityPct() { return humidityPct; }
    public Double getWindSpeedKph() { return windSpeedKph; }
    public Double getPressureHpa() { return pressureHpa; }
    public Integer getWeatherCode() { return weatherCode; }
    public String getDescription() { return description; }
}
