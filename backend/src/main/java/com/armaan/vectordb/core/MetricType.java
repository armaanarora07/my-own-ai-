package com.armaan.vectordb.core;

import java.util.Arrays;

/**
 * Enumeration of supported distance and similarity metrics.
 *
 * <p>Each enum value acts as a factory linking a user-friendly identifier
 * to its concrete {@link DistanceMetric} strategy implementation.
 */
public enum MetricType {
    COSINE("cosine", CosineSimilarity.INSTANCE),
    EUCLIDEAN("euclidean", EuclideanDistance.INSTANCE),
    MANHATTAN("manhattan", ManhattanDistance.INSTANCE);

    private final String identifier;
    private final DistanceMetric metricInstance;

    MetricType(String identifier, DistanceMetric metricInstance) {
        this.identifier = identifier;
        this.metricInstance = metricInstance;
    }

    public String getIdentifier() {
        return identifier;
    }

    public DistanceMetric getMetric() {
        return metricInstance;
    }

    /**
     * Resolves a string identifier to its matching {@link MetricType}.
     *
     * @param value user-provided string (e.g. "cosine", "Euclidean", "MANHATTAN")
     * @return the corresponding {@link MetricType}
     * @throws IllegalArgumentException if null or not recognized
     */
    public static MetricType fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Metric name cannot be null or empty. Supported metrics: " 
                    + Arrays.toString(MetricType.values()));
        }
        String normalized = value.trim().toLowerCase();
        for (MetricType type : MetricType.values()) {
            if (type.identifier.equalsIgnoreCase(normalized) || type.name().equalsIgnoreCase(normalized)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown metric: '" + value + "'. Supported metrics: " 
                + Arrays.toString(MetricType.values()));
    }
}
