package com.smartweather.exception;

/** Thrown when Open-Meteo is unreachable or returns an unexpected payload. → HTTP 502. */
public class ExternalWeatherApiException extends RuntimeException {

    public ExternalWeatherApiException(String message) {
        super(message);
    }

    public ExternalWeatherApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
