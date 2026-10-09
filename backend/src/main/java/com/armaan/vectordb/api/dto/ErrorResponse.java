package com.armaan.vectordb.api.dto;

import java.time.Instant;
import java.util.List;

/**
 * Standardized error payload returned to API clients.
 */
public record ErrorResponse(
        int status,
        String error,
        String message,
        List<String> details,
        String path,
        Instant timestamp
) {
    public ErrorResponse(int status, String error, String message, String path) {
        this(status, error, message, List.of(), path, Instant.now());
    }

    public ErrorResponse(int status, String error, String message, List<String> details, String path) {
        this(status, error, message, details, path, Instant.now());
    }
}
