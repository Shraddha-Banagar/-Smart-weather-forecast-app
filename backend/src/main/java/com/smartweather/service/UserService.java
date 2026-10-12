package com.smartweather.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartweather.dto.UserDtos.UserResponse;
import com.smartweather.entity.User;
import com.smartweather.exception.UserNotFoundException;
import com.smartweather.repository.UserRepository;

/**
 * Phase 2 mock auth: no passwords — login is find-or-create by username.
 */
@Service
public class UserService {

    private final UserRepository users;

    public UserService(UserRepository users) {
        this.users = users;
    }

    @Transactional
    public UserResponse login(String rawUsername) {
        if (rawUsername == null || rawUsername.isBlank()) {
            throw new IllegalArgumentException("username must not be blank");
        }
        var username = rawUsername.strip().toLowerCase();
        if (username.length() > 50) {
            throw new IllegalArgumentException("username must be at most 50 characters");
        }
        var user = users.findByUsername(username)
                .orElseGet(() -> users.save(new User(username, capitalize(rawUsername.strip()))));
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public User requireUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("user id must not be null");
        }
        return users.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }

    static UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getDisplayName(), user.getCreatedAt());
    }

    private static String capitalize(String s) {
        if (s.isEmpty()) {
            return s;
        }
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
