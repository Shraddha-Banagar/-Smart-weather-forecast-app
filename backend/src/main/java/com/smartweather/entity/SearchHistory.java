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

/**
 * Phase 2: every city search a user performs (latest first in queries).
 * Table: {@code search_history}.
 */
@Entity
@Table(name = "search_history")
public class SearchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected SearchHistory() {}

    public SearchHistory(User user, String city) {
        this.user = user;
        this.city = city;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getCity() { return city; }
    public Instant getCreatedAt() { return createdAt; }
}
