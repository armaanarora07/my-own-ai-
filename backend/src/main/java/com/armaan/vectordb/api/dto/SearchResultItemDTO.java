package com.armaan.vectordb.api.dto;

import java.util.Map;

/**
 * Individual matched item inside a search response.
 */
public record SearchResultItemDTO(
        int rank,
        String id,
        double score,
        String category,
        String metadata,
        float[] values,
        Map<String, Object> attributes
) {}
