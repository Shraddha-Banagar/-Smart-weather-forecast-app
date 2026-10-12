package com.smartweather.exception;

import java.time.Instant;
import java.util.stream.Collectors;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.client.RestClientException;

import com.smartweather.dto.ErrorResponse;

import jakarta.validation.ConstraintViolationException;

/**
 * Error handling: Level 1 (invalid city → 404, bad input → 400,
 * upstream failure → 502) + Phase 2 (unknown user → 404, duplicate favorite → 409).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCityNotFound(CityNotFoundException ex, WebRequest request) {
        return build(HttpStatus.NOT_FOUND, "City '%s' not found. Check the spelling and try again.".formatted(ex.getCity()), request);
    }

    @ExceptionHandler({ ConstraintViolationException.class, MethodArgumentTypeMismatchException.class })
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Invalid request: " + ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + " " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, details.isBlank() ? "Validation failed" : details, request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST,
                "Missing required query parameter '%s'. Example: ?city=London".formatted(ex.getParameterName()),
                request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleBadBody(HttpMessageNotReadableException ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST,
                "Malformed JSON request body. Check field names and types and try again.", request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleConflict(DataIntegrityViolationException ex, WebRequest request) {
        return build(HttpStatus.CONFLICT, "Request conflicts with existing data (it may already exist).", request);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDatabaseDown(DataAccessException ex, WebRequest request) {
        return build(HttpStatus.SERVICE_UNAVAILABLE,
                "User database is unavailable — start PostgreSQL with 'docker compose up -d' and try again.",
                request);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex, WebRequest request) {
        return build(HttpStatus.NOT_FOUND, "User '%s' not found. Log in again.".formatted(ex.getUserId()), request);
    }

    @ExceptionHandler(DuplicateFavoriteException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateFavorite(DuplicateFavoriteException ex, WebRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(ExternalWeatherApiException.class)
    public ResponseEntity<ErrorResponse> handleUpstream(ExternalWeatherApiException ex, WebRequest request) {
        return build(HttpStatus.BAD_GATEWAY, "Weather provider unavailable: " + ex.getMessage(), request);
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ErrorResponse> handleRestClient(RestClientException ex, WebRequest request) {
        return build(HttpStatus.BAD_GATEWAY, "Weather provider request failed: " + ex.getMessage(), request);
    }

    // Fallback for anything unexpected.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, WebRequest request) {
        // Java 21 pattern matching — distinguish IllegalArgumentException (bad input → 400).
        if (ex instanceof IllegalArgumentException illegal) {
            return build(HttpStatus.BAD_REQUEST, illegal.getMessage(), request);
        }
        String message = ex.getMessage() == null ? "Unexpected error" : ex.getMessage();
        return build(HttpStatus.INTERNAL_SERVER_ERROR, message, request);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");
        return ResponseEntity.status(status)
                .body(new ErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(), message, path));
    }
}
