package com.smartweather.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Modern Spring 6.1 {@link RestClient} (backed by {@code java.net.http}-friendly
 * request factory with explicit timeouts) for all outbound Open-Meteo calls.
 */
@Configuration
public class RestClientConfig {

    @Bean
    RestClient weatherRestClient(@Value("${app.weather.timeout-seconds:10}") int timeoutSeconds) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(timeoutSeconds));
        factory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));
        return RestClient.builder()
                .requestFactory(factory)
                .defaultHeader("Accept", "application/json")
                .defaultHeader("User-Agent", "smart-weather-app/0.1.0")
                .build();
    }
}
