package com.armaan.vectordb.service;

import com.armaan.vectordb.api.dto.AddVectorRequest;
import com.armaan.vectordb.api.dto.DatabaseStatsDTO;
import com.armaan.vectordb.api.dto.SearchRequest;
import com.armaan.vectordb.api.dto.SearchResponseDTO;
import com.armaan.vectordb.api.dto.SearchResultItemDTO;
import com.armaan.vectordb.core.MetricType;
import com.armaan.vectordb.core.VectorDB;
import com.armaan.vectordb.core.VectorNotFoundException;
import com.armaan.vectordb.model.VectorRecord;
import com.armaan.vectordb.search.SearchResult;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service orchestrating application-level vector operations.
 *
 * <p>Bridges the REST API controllers and the pure {@link VectorDB} core engine.
 */
@Service
public class VectorService {

    private final VectorDB vectorDB;

    public VectorService(VectorDB vectorDB) {
        this.vectorDB = vectorDB;
    }

    /**
     * Stores a new vector record.
     */
    public VectorRecord addVector(AddVectorRequest request) {
        VectorRecord record = new VectorRecord(
                request.id(),
                request.values(),
                request.category(),
                request.metadata(),
                request.attributes()
        );
        vectorDB.insert(record);
        return record;
    }

    /**
     * Retrieves a vector record by ID.
     *
     * @throws VectorNotFoundException if not found
     */
    public VectorRecord getVector(String id) {
        return vectorDB.get(id)
                .orElseThrow(() -> new VectorNotFoundException(id));
    }

    /**
     * Returns all stored vector records.
     */
    public List<VectorRecord> getAllVectors() {
        return vectorDB.getAll().stream()
                .sorted(Comparator.comparing(VectorRecord::getId))
                .collect(Collectors.toList());
    }

    /**
     * Deletes a vector record by ID.
     *
     * @throws VectorNotFoundException if no vector with the specified ID exists
     */
    public void deleteVector(String id) {
        boolean removed = vectorDB.delete(id);
        if (!removed) {
            throw new VectorNotFoundException(id);
        }
    }

    /**
     * Executes similarity search and measures execution latency.
     */
    public SearchResponseDTO search(SearchRequest request) {
        MetricType metricType = MetricType.fromString(request.metric());

        long startNanos = System.nanoTime();
        List<SearchResult> hits = vectorDB.search(request.query(), request.k(), metricType);
        long elapsedNanos = System.nanoTime() - startNanos;

        long elapsedMicros = elapsedNanos / 1_000;
        double elapsedMs = elapsedNanos / 1_000_000.0;

        List<SearchResultItemDTO> resultItems = hits.stream()
                .map(hit -> new SearchResultItemDTO(
                        hit.rank(),
                        hit.vector().getId(),
                        hit.score(),
                        hit.vector().getCategory(),
                        hit.vector().getMetadata(),
                        hit.vector().getValues(),
                        hit.vector().getAttributes()
                ))
                .toList();

        return new SearchResponseDTO(
                resultItems,
                elapsedMicros,
                elapsedMs,
                metricType.getIdentifier(),
                vectorDB.getSearchAlgorithmName(),
                vectorDB.size()
        );
    }

    /**
     * Returns database metadata and configuration statistics.
     */
    public DatabaseStatsDTO getStats() {
        List<String> supportedMetrics = Arrays.stream(MetricType.values())
                .map(MetricType::getIdentifier)
                .toList();

        return new DatabaseStatsDTO(
                vectorDB.size(),
                vectorDB.getDimensions(),
                vectorDB.getSearchAlgorithmName(),
                supportedMetrics
        );
    }
}
