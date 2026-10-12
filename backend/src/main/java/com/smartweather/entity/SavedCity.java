package com.smartweather.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Phase 2: a city pinned by a user to their dashboard.
 * Table: {@code saved_cities}. One row per (user, city-normalized).
 */
@Entity
@Table(name = "saved_cities", uniqueConstraints = {
        @UniqueConstraint(name = "uq_saved_cities_user_city", columnNames = { "user_id", "city_normalized" })
})
public class SavedCity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(name = "city_normalized", nullable = false, length = 100)
    private String cityNormalized;

    @Column(length = 100)
    private String country;

    private Double latitude;

    private Double longitude;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected SavedCity() {}

    public SavedCity(User user, String city, String country, Double latitude, Double longitude) {
        this.user = user;
        this.city = city;
        this.cityNormalized = city.strip().toLowerCase();
        this.country = country;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (cityNormalized == null && city != null) {
            cityNormalized = city.strip().toLowerCase();
        }
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getCity() { return city; }
    public String getCityNormalized() { return cityNormalized; }
    public String getCountry() { return country; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public Instant getCreatedAt() { return createdAt; }
}
