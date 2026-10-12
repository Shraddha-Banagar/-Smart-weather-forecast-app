package com.smartweather.exception;

/** Thrown when geocoding finds no match for the requested city. → HTTP 404. */
public class CityNotFoundException extends RuntimeException {

    private final String city;

    public CityNotFoundException(String city) {
        super("City not found: '%s'".formatted(city));
        this.city = city;
    }

    public String getCity() {
        return city;
    }
}
