package com.smartweather.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartweather.dto.UserDtos.SearchHistoryResponse;
import com.smartweather.entity.SearchHistory;
import com.smartweather.repository.SearchHistoryRepository;

/**
 * Phase 2: records each weather lookup per user; latest-first reads.
 */
@Service
public class SearchHistoryService {

    private final SearchHistoryRepository history;
    private final UserService userService;

    public SearchHistoryService(SearchHistoryRepository history, UserService userService) {
        this.history = history;
        this.userService = userService;
    }

    @Transactional
    public void record(Long userId, String city) {
        if (userId == null || city == null || city.isBlank()) {
            return; // anonymous or invalid searches are simply not tracked
        }
        var user = userService.requireUser(userId);
        history.save(new SearchHistory(user, city.strip()));
    }

    @Transactional(readOnly = true)
    public List<SearchHistoryResponse> list(Long userId) {
        userService.requireUser(userId);
        return history.findTop50ByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(e -> new SearchHistoryResponse(e.getId(), e.getCity(), e.getCreatedAt()))
                .toList();
    }

    @Transactional
    public void clear(Long userId) {
        userService.requireUser(userId);
        history.deleteByUserId(userId);
    }
}
