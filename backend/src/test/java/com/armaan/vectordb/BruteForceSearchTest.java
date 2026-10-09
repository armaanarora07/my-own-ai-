package com.armaan.vectordb;

import com.armaan.vectordb.core.CosineSimilarity;
import com.armaan.vectordb.core.EuclideanDistance;
import com.armaan.vectordb.model.VectorRecord;
import com.armaan.vectordb.search.BruteForceSearch;
import com.armaan.vectordb.search.SearchResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class BruteForceSearchTest {

    private final BruteForceSearch searchEngine = BruteForceSearch.INSTANCE;

    @Test
    @DisplayName("Top-K search with Euclidean distance ranks closest vectors first")
    void testEuclideanTopKRanking() {
        // Stored vectors in 2D
        VectorRecord v1 = new VectorRecord("v1", new float[]{1.0f, 1.0f}, "test", "v1 close to origin");
        VectorRecord v2 = new VectorRecord("v2", new float[]{5.0f, 5.0f}, "test", "v2 far");
        VectorRecord v3 = new VectorRecord("v3", new float[]{0.5f, 0.5f}, "test", "v3 closest to origin");
        VectorRecord v4 = new VectorRecord("v4", new float[]{10.0f, 10.0f}, "test", "v4 very far");

        List<VectorRecord> corpus = List.of(v1, v2, v3, v4);

        // Query at (0.0, 0.0), request top K=2
        float[] query = {0.0f, 0.0f};
        List<SearchResult> results = searchEngine.search(query, 2, EuclideanDistance.INSTANCE, corpus);

        assertThat(results).hasSize(2);

        // Rank 1: v3 (distance sqrt(0.5^2 + 0.5^2) = sqrt(0.5) ~ 0.7071)
        assertThat(results.get(0).rank()).isEqualTo(1);
        assertThat(results.get(0).vector().getId()).isEqualTo("v3");
        assertThat(results.get(0).score()).isCloseTo(Math.sqrt(0.5), within(1e-4));

        // Rank 2: v1 (distance sqrt(1^2 + 1^2) = sqrt(2) ~ 1.4142)
        assertThat(results.get(1).rank()).isEqualTo(2);
        assertThat(results.get(1).vector().getId()).isEqualTo("v1");
        assertThat(results.get(1).score()).isCloseTo(Math.sqrt(2.0), within(1e-4));
    }

    @Test
    @DisplayName("Top-K search with Cosine similarity ranks highest similarity first")
    void testCosineTopKRanking() {
        // Query points along X axis: (1.0, 0.0)
        float[] query = {1.0f, 0.0f};

        VectorRecord parallel = new VectorRecord("parallel", new float[]{5.0f, 0.0f}, "test", "angle 0 deg");
        VectorRecord fortyFive = new VectorRecord("fortyFive", new float[]{1.0f, 1.0f}, "test", "angle 45 deg");
        VectorRecord orthogonal = new VectorRecord("orthogonal", new float[]{0.0f, 1.0f}, "test", "angle 90 deg");
        VectorRecord opposite = new VectorRecord("opposite", new float[]{-1.0f, 0.0f}, "test", "angle 180 deg");

        List<VectorRecord> corpus = List.of(parallel, fortyFive, orthogonal, opposite);

        List<SearchResult> results = searchEngine.search(query, 4, CosineSimilarity.INSTANCE, corpus);

        assertThat(results).hasSize(4);

        // Rank 1: parallel (similarity 1.0)
        assertThat(results.get(0).vector().getId()).isEqualTo("parallel");
        assertThat(results.get(0).score()).isCloseTo(1.0, within(1e-4));

        // Rank 2: 45 degrees (similarity ~ 0.7071)
        assertThat(results.get(1).vector().getId()).isEqualTo("fortyFive");
        assertThat(results.get(1).score()).isCloseTo(1.0 / Math.sqrt(2), within(1e-4));

        // Rank 3: orthogonal (similarity 0.0)
        assertThat(results.get(2).vector().getId()).isEqualTo("orthogonal");
        assertThat(results.get(2).score()).isCloseTo(0.0, within(1e-4));

        // Rank 4: opposite (similarity -1.0)
        assertThat(results.get(3).vector().getId()).isEqualTo("opposite");
        assertThat(results.get(3).score()).isCloseTo(-1.0, within(1e-4));
    }

    @Test
    @DisplayName("Search gracefully handles K greater than corpus size")
    void testKGreaterThanCorpusSize() {
        VectorRecord v1 = new VectorRecord("v1", new float[]{1.0f, 2.0f});
        VectorRecord v2 = new VectorRecord("v2", new float[]{3.0f, 4.0f});

        List<SearchResult> results = searchEngine.search(
                new float[]{1.0f, 2.0f}, 10, EuclideanDistance.INSTANCE, List.of(v1, v2));

        assertThat(results).hasSize(2);
        assertThat(results.get(0).rank()).isEqualTo(1);
        assertThat(results.get(1).rank()).isEqualTo(2);
    }

    @Test
    @DisplayName("Invalid K values throw IllegalArgumentException")
    void testInvalidK() {
        VectorRecord v1 = new VectorRecord("v1", new float[]{1.0f, 2.0f});
        assertThatThrownBy(() -> searchEngine.search(new float[]{1.0f, 2.0f}, 0, EuclideanDistance.INSTANCE, List.of(v1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be greater than zero");
    }

    @Test
    @DisplayName("Empty corpus returns empty results list")
    void testEmptyCorpus() {
        List<SearchResult> results = searchEngine.search(new float[]{1.0f, 2.0f}, 5, EuclideanDistance.INSTANCE, List.of());
        assertThat(results).isEmpty();
    }
}
