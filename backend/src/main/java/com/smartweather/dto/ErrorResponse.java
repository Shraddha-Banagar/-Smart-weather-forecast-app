package com.smartweather.dto;

import java.time.Instant;

/** Uniform error body returned by {@code GlobalExceptionHandler}. */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {}
