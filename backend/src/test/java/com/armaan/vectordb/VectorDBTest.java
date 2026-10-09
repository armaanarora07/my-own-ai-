package com.armaan.vectordb;

import com.armaan.vectordb.core.DimensionMismatchException;
import com.armaan.vectordb.core.MetricType;
import com.armaan.vectordb.core.VectorDB;
import com.armaan.vectordb.model.VectorRecord;
import com.armaan.vectordb.search.SearchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VectorDBTest {

    private VectorDB vectorDB;

    @BeforeEach
    void setUp() {
        // Initialize 3-dimensional VectorDB
        vectorDB = new VectorDB(3);
    }

    @Test
    @DisplayName("Insert, retrieve, and delete vectors successfully")
    void testBasicCrud() {
        VectorRecord record = new VectorRecord("vec-1", new float[]{1.0f, 2.0f, 3.0f}, "test", "demo record");

        assertThat(vectorDB.size()).isEqualTo(0);
        assertThat(vectorDB.isEmpty()).isTrue();

        vectorDB.insert(record);

        assertThat(vectorDB.size()).isEqualTo(1);
        assertThat(vectorDB.isEmpty()).isFalse();

        Optional<VectorRecord> retrieved = vectorDB.get("vec-1");
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getId()).isEqualTo("vec-1");
        assertThat(retrieved.get().getValues()).containsExactly(1.0f, 2.0f, 3.0f);
        assertThat(retrieved.get().getCategory()).isEqualTo("test");
        assertThat(retrieved.get().getMetadata()).isEqualTo("demo record");

        // Delete vector
        boolean deleted = vectorDB.delete("vec-1");
        assertThat(deleted).isTrue();
        assertThat(vectorDB.size()).isEqualTo(0);
        assertThat(vectorDB.get("vec-1")).isEmpty();

        // Delete non-existent vector returns false
        assertThat(vectorDB.delete("non-existent")).isFalse();
    }

    @Test
    @DisplayName("Insert rejects vectors with incorrect dimensionality")
    void testDimensionMismatchOnInsert() {
        // Vector with 2 dimensions inserted into 3D database
        VectorRecord badRecord = new VectorRecord("bad", new float[]{1.0f, 2.0f});

        assertThatThrownBy(() -> vectorDB.insert(badRecord))
                .isInstanceOf(DimensionMismatchException.class)
                .hasMessageContaining("expected 3 dimensions, but received 2");
    }

    @Test
    @DisplayName("Search delegates correctly to search algorithm and returns ranked results")
    void testSearchDelegation() {
        vectorDB.insert(new VectorRecord("v1", new float[]{1.0f, 0.0f, 0.0f}, "axis", "X-axis"));
        vectorDB.insert(new VectorRecord("v2", new float[]{0.0f, 1.0f, 0.0f}, "axis", "Y-axis"));
        vectorDB.insert(new VectorRecord("v3", new float[]{0.0f, 0.0f, 1.0f}, "axis", "Z-axis"));

        float[] query = {0.9f, 0.1f, 0.0f};

        List<SearchResult> results = vectorDB.search(query, 2, MetricType.COSINE);

        assertThat(results).hasSize(2);
        // Best match should be v1
        assertThat(results.get(0).vector().getId()).isEqualTo("v1");
        assertThat(results.get(0).rank()).isEqualTo(1);
        assertThat(results.get(1).rank()).isEqualTo(2);
    }
}
