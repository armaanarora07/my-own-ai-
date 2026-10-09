package com.armaan.vectordb.api;

import com.armaan.vectordb.api.dto.AddVectorRequest;
import com.armaan.vectordb.api.dto.DatabaseStatsDTO;
import com.armaan.vectordb.api.dto.SearchRequest;
import com.armaan.vectordb.api.dto.SearchResponseDTO;
import com.armaan.vectordb.model.VectorRecord;
import com.armaan.vectordb.service.VectorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST API Controller exposing VectorDB operations over HTTP.
 */
@RestController
@RequestMapping("/api/vectors")
@CrossOrigin(origins = "*") // Allows access from web frontends
public class VectorController {

    private final VectorService vectorService;

    public VectorController(VectorService vectorService) {
        this.vectorService = vectorService;
    }

    /**
     * Retrieves database metadata and runtime statistics.
     */
    @GetMapping("/stats")
    public ResponseEntity<DatabaseStatsDTO> getStats() {
        return ResponseEntity.ok(vectorService.getStats());
    }

    /**
     * Retrieves all stored vector records.
     */
    @GetMapping
    public ResponseEntity<List<VectorRecord>> getAllVectors() {
        return ResponseEntity.ok(vectorService.getAllVectors());
    }

    /**
     * Retrieves a single vector record by its ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<VectorRecord> getVectorById(@PathVariable String id) {
        return ResponseEntity.ok(vectorService.getVector(id));
    }

    /**
     * Inserts or updates a vector in the database.
     */
    @PostMapping
    public ResponseEntity<VectorRecord> addVector(@Valid @RequestBody AddVectorRequest request) {
        VectorRecord created = vectorService.addVector(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Deletes a vector record by ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteVector(@PathVariable String id) {
        vectorService.deleteVector(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Vector '" + id + "' deleted successfully"
        ));
    }

    /**
     * Executes nearest-neighbor similarity search.
     */
    @PostMapping("/search")
    public ResponseEntity<SearchResponseDTO> search(@Valid @RequestBody SearchRequest request) {
        return ResponseEntity.ok(vectorService.search(request));
    }
}
