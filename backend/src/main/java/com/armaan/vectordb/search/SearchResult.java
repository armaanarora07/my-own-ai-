package com.armaan.vectordb.search;

import com.armaan.vectordb.model.VectorRecord;
import java.util.Objects;

/**
 * Represents a single ranked search result item.
 *
 * @param vector the matched {@link VectorRecord} from the database
 * @param score  the distance or similarity score computed against the query vector
 * @param rank   the 1-based ranking position (1 is the best match)
 */
public record SearchResult(
        VectorRecord vector,
        double score,
        int rank
) {
    public SearchResult {
        Objects.requireNonNull(vector, "Matched vector cannot be null");
        if (rank < 1) {
            throw new IllegalArgumentException("Rank must be positive (>= 1)");
        }
    }
}
