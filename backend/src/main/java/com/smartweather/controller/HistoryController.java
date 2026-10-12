package com.smartweather.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartweather.dto.UserDtos.SearchHistoryResponse;
import com.smartweather.service.SearchHistoryService;

/**
 * Phase 2 search history: latest-first reads + clear.
 */
@Validated
@RestController
@RequestMapping("/api/users/{userId}/history")
public class HistoryController {

    private final SearchHistoryService history;

    public HistoryController(SearchHistoryService history) {
        this.history = history;
    }

    @GetMapping
    public ResponseEntity<List<SearchHistoryResponse>> list(@PathVariable Long userId) {
        return ResponseEntity.ok(history.list(userId));
    }

    @DeleteMapping
    public ResponseEntity<Void> clear(@PathVariable Long userId) {
        history.clear(userId);
        return ResponseEntity.noContent().build();
    }
}
