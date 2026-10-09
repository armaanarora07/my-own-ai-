package com.armaan.vectordb.core;

import com.armaan.vectordb.model.VectorRecord;
import com.armaan.vectordb.search.BruteForceSearch;
import com.armaan.vectordb.search.SearchAlgorithm;
import com.armaan.vectordb.search.SearchResult;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Core In-Memory Vector Database engine.
 *
 * <p><b>Responsibilities:</b>
 * <ul>
 *   <li>Enforces uniform vector dimensionality across all stored records.</li>
 *   <li>Provides thread-safe CRUD storage operations using a {@link ConcurrentHashMap}.</li>
 *   <li>Delegates nearest-neighbor search to a pluggable {@link SearchAlgorithm} strategy.</li>
 *   <li>Maintains complete independence from web, persistence, and framework frameworks.</li>
 * </ul>
 */
public class VectorDB {

    private final int dimensions;
    private final ConcurrentMap<String, VectorRecord> storage;
    private volatile SearchAlgorithm searchAlgorithm;

    /**
     * Initializes the vector database with a fixed dimensionality and default Brute Force search.
     *
     * @param dimensions expected dimensionality for all vectors in this database
     */
    public VectorDB(int dimensions) {
        this(dimensions, BruteForceSearch.INSTANCE);
    }

    /**
     * Initializes the vector database with a fixed dimensionality and a custom search algorithm.
     *
     * @param dimensions      expected dimensionality for all vectors in this database
     * @param searchAlgorithm algorithm implementation (e.g. BruteForceSearch, KDTree, HNSW)
     */
    public VectorDB(int dimensions, SearchAlgorithm searchAlgorithm) {
        if (dimensions <= 0) {
            throw new IllegalArgumentException("Vector database dimensions must be positive (> 0), received: " + dimensions);
        }
        this.dimensions = dimensions;
        this.storage = new ConcurrentHashMap<>();
        this.searchAlgorithm = Objects.requireNonNull(searchAlgorithm, "Search algorithm cannot be null");
    }

    /**
     * Inserts or updates (upserts) a vector in the database.
     *
     * @param record the vector record to store
     * @throws IllegalArgumentException if record is null
     * @throws DimensionMismatchException if vector dimensions do not match database configuration
     */
    public void insert(VectorRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("Vector record cannot be null");
        }
        if (record.getDimensions() != this.dimensions) {
            throw new DimensionMismatchException(this.dimensions, record.getDimensions());
        }
        storage.put(record.getId(), record);
    }

    /**
     * Retrieves a vector by its unique identifier.
     *
     * @param id unique vector ID
     * @return an {@link Optional} containing the record if found, or empty
     */
    public Optional<VectorRecord> get(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(storage.get(id));
    }

    /**
     * Removes a vector by its ID.
     *
     * @param id unique vector ID
     * @return {@code true} if an item was removed, {@code false} if no matching ID existed
     */
    public boolean delete(String id) {
        if (id == null) {
            return false;
        }
        return storage.remove(id) != null;
    }

    /**
     * Returns the total count of vectors stored.
     */
    public int size() {
        return storage.size();
    }

    /**
     * Returns true if database contains no vectors.
     */
    public boolean isEmpty() {
        return storage.isEmpty();
    }

    /**
     * Returns an unmodifiable collection of all stored vectors.
     */
    public Collection<VectorRecord> getAll() {
        return Collections.unmodifiableCollection(storage.values());
    }

    /**
     * Clears all vectors from the database.
     */
    public void clear() {
        storage.clear();
    }

    /**
     * Searches for top-K nearest neighbors using the specified {@link MetricType}.
     *
     * @param queryVector numerical vector query
     * @param k           maximum number of neighbors to return
     * @param metricType  the metric enum (e.g. COSINE, EUCLIDEAN, MANHATTAN)
     * @return ordered list of ranked search results
     */
    public List<SearchResult> search(float[] queryVector, int k, MetricType metricType) {
        if (metricType == null) {
            throw new IllegalArgumentException("MetricType cannot be null");
        }
        return search(queryVector, k, metricType.getMetric());
    }

    /**
     * Searches for top-K nearest neighbors using a custom {@link DistanceMetric} strategy.
     *
     * @param queryVector numerical vector query
     * @param k           maximum number of neighbors to return
     * @param metric      concrete metric strategy
     * @return ordered list of ranked search results
     */
    public List<SearchResult> search(float[] queryVector, int k, DistanceMetric metric) {
        if (queryVector == null) {
            throw new IllegalArgumentException("Query vector cannot be null");
        }
        if (queryVector.length != this.dimensions) {
            throw new DimensionMismatchException(this.dimensions, queryVector.length);
        }
        if (k <= 0) {
            throw new IllegalArgumentException("k must be greater than zero, received: " + k);
        }
        if (metric == null) {
            throw new IllegalArgumentException("DistanceMetric cannot be null");
        }

        return searchAlgorithm.search(queryVector, k, metric, storage.values());
    }

    public int getDimensions() {
        return dimensions;
    }

    public String getSearchAlgorithmName() {
        return searchAlgorithm.getName();
    }

    public void setSearchAlgorithm(SearchAlgorithm searchAlgorithm) {
        this.searchAlgorithm = Objects.requireNonNull(searchAlgorithm, "Search algorithm cannot be null");
    }
}
