package com.armaan.vectordb.search;

import com.armaan.vectordb.core.DistanceMetric;
import com.armaan.vectordb.model.VectorRecord;
import java.util.Collection;
import java.util.List;

/**
 * Strategy interface representing a vector search algorithm.
 *
 * <p>Phase 1 provides {@link BruteForceSearch} (exact k-NN).
 * Phase 2 will implement additional index-backed algorithms such as KD-Tree and HNSW
 * conforming to this contract without requiring architectural changes to {@code VectorDB}.
 */
public interface SearchAlgorithm {

    /**
     * Unique identifier for this search algorithm (e.g. "bruteforce", "kdtree", "hnsw").
     */
    String getName();

    /**
     * Executes a top-K nearest-neighbor search against the corpus of stored vectors.
     *
     * @param queryVector the vector to search against
     * @param k           the maximum number of results to return (k > 0)
     * @param metric      the distance or similarity metric to evaluate
     * @param corpus      the candidate collection of vectors to search through
     * @return a ranked list of {@link SearchResult} objects, ordered from best match to worst match
     */
    List<SearchResult> search(float[] queryVector, int k, DistanceMetric metric, Collection<VectorRecord> corpus);
}
