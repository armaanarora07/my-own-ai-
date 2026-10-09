package com.armaan.vectordb.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents an individual vector entity in the vector database.
 *
 * <p>Each vector record contains:
 * <ul>
 *   <li><b>id</b>: A unique string identifier.</li>
 *   <li><b>values</b>: A primitive {@code float[]} array containing the numerical embedding.</li>
 *   <li><b>category</b>: An optional semantic category or cluster tag (e.g. "cs", "math").</li>
 *   <li><b>metadata</b>: An optional descriptive text or document snippet.</li>
 *   <li><b>attributes</b>: An unmodifiable map of custom key-value pairs for flexible attributes.</li>
 * </ul>
 *
 * <p><b>Thread-Safety & Immutability:</b>
 * The underlying array is defensively copied during construction and retrieval to prevent
 * external mutation. The attributes map is wrapped in an unmodifiable map.
 */
public final class VectorRecord {

    private final String id;
    private final float[] values;
    private final String category;
    private final String metadata;
    private final Map<String, Object> attributes;

    public VectorRecord(String id, float[] values, String category, String metadata, Map<String, Object> attributes) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Vector ID cannot be null or blank");
        }
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("Vector values cannot be null or empty");
        }
        this.id = id.trim();
        // Defensive copy: prevents caller from mutating our internal state via their array reference
        this.values = values.clone();
        this.category = category != null ? category.trim() : "";
        this.metadata = metadata != null ? metadata.trim() : "";
        this.attributes = attributes != null 
                ? Collections.unmodifiableMap(new HashMap<>(attributes)) 
                : Collections.emptyMap();
    }

    public VectorRecord(String id, float[] values, String category, String metadata) {
        this(id, values, category, metadata, Collections.emptyMap());
    }

    public VectorRecord(String id, float[] values) {
        this(id, values, "", "", Collections.emptyMap());
    }

    public String getId() {
        return id;
    }

    /**
     * Returns a defensive copy of the vector's float array.
     */
    public float[] getValues() {
        return values.clone();
    }

    public int getDimensions() {
        return values.length;
    }

    public String getCategory() {
        return category;
    }

    public String getMetadata() {
        return metadata;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VectorRecord that)) return false;
        return Objects.equals(id, that.id) &&
               Arrays.equals(values, that.values) &&
               Objects.equals(category, that.category) &&
               Objects.equals(metadata, that.metadata) &&
               Objects.equals(attributes, that.attributes);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, category, metadata, attributes);
        result = 31 * result + Arrays.hashCode(values);
        return result;
    }

    @Override
    public String toString() {
        return "VectorRecord{" +
                "id='" + id + '\'' +
                ", dimensions=" + values.length +
                ", category='" + category + '\'' +
                ", metadata='" + metadata + '\'' +
                '}';
    }
}
