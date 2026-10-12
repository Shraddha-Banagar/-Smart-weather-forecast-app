package com.smartweather.exception;

/** Thrown when a favorite already exists (or another uniqueness rule trips). → HTTP 409. */
public class DuplicateFavoriteException extends RuntimeException {

    public DuplicateFavoriteException(String message) {
        super(message);
    }
}
