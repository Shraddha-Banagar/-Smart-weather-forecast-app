package com.smartweather.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartweather.dto.UserDtos.SaveCityRequest;
import com.smartweather.dto.UserDtos.SavedCityResponse;
import com.smartweather.service.FavoritesService;

import jakarta.validation.Valid;

/**
 * Phase 2 favorites: pin / unpin cities on the user's dashboard.
 */
@Validated
@RestController
@RequestMapping("/api/users/{userId}/favorites")
public class FavoritesController {

    private final FavoritesService favorites;

    public FavoritesController(FavoritesService favorites) {
        this.favorites = favorites;
    }

    @GetMapping
    public ResponseEntity<List<SavedCityResponse>> list(@PathVariable Long userId) {
        return ResponseEntity.ok(favorites.list(userId));
    }

    @PostMapping
    public ResponseEntity<SavedCityResponse> add(
            @PathVariable Long userId,
            @Valid @RequestBody SaveCityRequest request) {
        String city = request == null ? null : request.city();
        return ResponseEntity.ok(favorites.add(userId, city));
    }

    @DeleteMapping("/{city}")
    public ResponseEntity<Void> remove(@PathVariable Long userId, @PathVariable String city) {
        favorites.remove(userId, city);
        return ResponseEntity.noContent().build();
    }
}
