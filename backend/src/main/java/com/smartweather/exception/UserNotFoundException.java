package com.smartweather.exception;

/** Thrown when a {@code X-User-Id} / path user id has no matching row. → HTTP 404. */
public class UserNotFoundException extends RuntimeException {

    private final Long userId;

    public UserNotFoundException(Long userId) {
        super("User not found: '%s'".formatted(userId));
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }
}
