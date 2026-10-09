package com.armaan.vectordb.api.dto;

import java.util.List;

/**
 * Top-level response returned by the search endpoint.
 */
public record SearchResponseDTO(
        List<SearchResultItemDTO> results,
        long latencyMicros,
        double latencyMs,
        String metric,
        String algorithm,
        int totalCorpusSize
) {}
