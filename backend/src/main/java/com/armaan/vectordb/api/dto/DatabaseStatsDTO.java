package com.armaan.vectordb.api.dto;

import java.util.List;

/**
 * Summary metrics and configuration statistics of the vector database.
 */
public record DatabaseStatsDTO(
        int totalVectors,
        int dimensions,
        String searchAlgorithm,
        List<String> supportedMetrics
) {}
