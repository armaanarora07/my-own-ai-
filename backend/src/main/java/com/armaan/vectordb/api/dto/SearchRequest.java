package com.armaan.vectordb.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

/**
 * Request payload for nearest-neighbor similarity search.
 */
public record SearchRequest(
        @NotEmpty(message = "Query vector array cannot be empty")
        float[] query,

        @Min(value = 1, message = "k must be at least 1")
        @Max(value = 1000, message = "k cannot exceed 1000")
        int k,

        String metric
) {
    public SearchRequest {
        if (metric == null || metric.trim().isEmpty()) {
            metric = "cosine";
        }
    }
}
